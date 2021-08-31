package de.dominikdassow.musicrs.task;

import de.dominikdassow.musicrs.model.SimilarTracksList;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationAlgorithm;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationProblem;
import de.dominikdassow.musicrs.recommender.algorithm.AlgorithmConfiguration;
import de.dominikdassow.musicrs.recommender.engine.SimilarTracksEngine;
import de.dominikdassow.musicrs.service.DatabaseService;
import de.dominikdassow.musicrs.study.ExecuteAlgorithms;
import de.dominikdassow.musicrs.study.GenerateTrackIdsForBestParetoSets;
import lombok.extern.slf4j.Slf4j;
import org.uma.jmetal.lab.experiment.Experiment;
import org.uma.jmetal.lab.experiment.ExperimentBuilder;
import org.uma.jmetal.lab.experiment.component.impl.ComputeQualityIndicators;
import org.uma.jmetal.lab.experiment.component.impl.GenerateReferenceParetoFront;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentProblem;
import org.uma.jmetal.qualityindicator.impl.Epsilon;
import org.uma.jmetal.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import org.uma.jmetal.solution.Solution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
public class ConductStudyTask
    extends Task {

    private static final String STUDY_NAME = "MusicPlaylistContinuationStudy";
    private static final String STUDY_DIRECTORY = "../data/study";

    private static final int MAX_RETRIES = 10;
    private static final int INDEPENDENT_RUNS = 2; // TODO

    private SimilarTracksEngine similarTracksEngine;

    private Set<Integer> playlists;

    private List<AlgorithmConfiguration<? extends Solution<Integer>>> algorithmConfigurations;

    public ConductStudyTask() {
        super("Conduct Study");
    }

    public ConductStudyTask forPlaylists(Integer... playlist) {
        this.playlists = Set.of(playlist);

        return this;
    }

    public ConductStudyTask using(List<AlgorithmConfiguration<? extends Solution<Integer>>> configurations) {
        this.algorithmConfigurations = configurations;

        return this;
    }

    @Override
    protected void init() {
        similarTracksEngine = new SimilarTracksEngine();
    }

    @Override
    protected void execute() throws IOException {
        log.info("NUMBER OF PROCESSORS: " + Runtime.getRuntime().availableProcessors());
        log.info("NUMBER OF THREADS: " + ForkJoinPool.commonPool().getParallelism());

        Map<String, ExperimentProblem<Solution<Integer>>> problems
            = new ConcurrentHashMap<>();

        List<ExperimentAlgorithm<Solution<Integer>, List<Solution<Integer>>>> algorithms
            = new ArrayList<>();

        getPlaylists().parallel().forEach(playlist -> {
            MusicPlaylistContinuationProblem.Configuration problemConfiguration
                = getProblemConfiguration(playlist);

            String problemTag = "MPC_" + playlist;

            algorithmConfigurations.forEach(algorithmConfiguration -> {
                @SuppressWarnings("unchecked")
                MusicPlaylistContinuationAlgorithm<Solution<Integer>> algorithm
                    = (MusicPlaylistContinuationAlgorithm<Solution<Integer>>)
                    algorithmConfiguration.createAlgorithmFor(problemConfiguration);

                problems.computeIfAbsent(problemTag, tag -> {
                    ExperimentProblem<Solution<Integer>> problem
                        = new ExperimentProblem<>(algorithm.getProblem(), tag);

                    problem.setReferenceFront(tag + ".csv");

                    return problem;
                });

                IntStream.range(0, INDEPENDENT_RUNS).forEach(run -> {
                    ExperimentAlgorithm<Solution<Integer>, List<Solution<Integer>>> a
                        = new ExperimentAlgorithm<>(
                        algorithm.get(),
                        algorithmConfiguration.getName(),
                        problems.get(problemTag),
                        run
                    );

                    algorithms.add(a);
                });
            });
        });

        Experiment<Solution<Integer>, List<Solution<Integer>>> experiment =
            new ExperimentBuilder<Solution<Integer>, List<Solution<Integer>>>(STUDY_NAME)
                .setAlgorithmList(algorithms)
                .setProblemList(new ArrayList<>(problems.values()))
                .setExperimentBaseDirectory(STUDY_DIRECTORY)
                .setOutputParetoFrontFileName("FUN")
                .setOutputParetoSetFileName("VAR")
                .setReferenceFrontDirectory(STUDY_DIRECTORY + "/" + STUDY_NAME + "/referenceFronts")
                .setIndicatorList(List.of(
                    // TODO: Check used indicators
                    // new NormalizedHypervolume<>(),
                    new PISAHypervolume<>(),
                    new Epsilon<>()
                ))
                .setIndependentRuns(INDEPENDENT_RUNS)
                .build();

        new ExecuteAlgorithms<>(experiment, MAX_RETRIES).run();
        new GenerateReferenceParetoFront(experiment).run();
        new ComputeQualityIndicators<>(experiment).run();
        new GenerateTrackIdsForBestParetoSets(experiment).run();
        // TODO: Check study evaluations
//        new GenerateFriedmanTestTables<>(experiment).run();
//        new GenerateLatexTablesWithStatistics(experiment).run();
//        new StudyVisualizer(STUDY_DIRECTORY + "/" + STUDY_NAME, StudyVisualizer.TYPE_OF_FRONT_TO_SHOW.MEDIAN)
//            .createHTMLPageForEachIndicator();
    }

    private Stream<Integer> getPlaylists() {
        return this.playlists == null ? DatabaseService.readAllPlaylistChallenges() : this.playlists.stream();
    }

    private MusicPlaylistContinuationProblem.Configuration getProblemConfiguration(Integer playlist) {
        List<SimilarTracksList> similarTracksLists
            = DatabaseService.readSimilarTracksLists(playlist);

        long numberOfUniqueTracks = similarTracksLists.stream()
            .flatMap(similarTracksList -> similarTracksList.getTracks().stream())
            .distinct()
            .count();

        // TODO: Constant
        if (numberOfUniqueTracks < 500) {
            log.warn("# [" + playlist + "] SIMILAR PLAYLISTS :: " +
                "Not enough unique tracks: " + numberOfUniqueTracks);

            return null;
        }

//        log.info("# [" + playlist + "] SIMILAR PLAYLISTS :: "
//            + similarTracksLists.size() + " :: "
//            + similarTracksLists.stream()
//            .flatMap(similarTracksList -> similarTracksList.getTracks().stream())
//            .distinct()
//            .count());

        Map<Integer, String> tracks
            = DatabaseService.readPlaylistTracks(playlist);

        return new MusicPlaylistContinuationProblem.Configuration(similarTracksEngine, similarTracksLists, tracks);
    }
}