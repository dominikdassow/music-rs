package de.dominikdassow.musicrs.task;

import com.fasterxml.jackson.databind.JsonNode;
import de.dominikdassow.musicrs.AppConfiguration;
import de.dominikdassow.musicrs.model.SimilarTracksList;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationAlgorithm;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationProblem;
import de.dominikdassow.musicrs.recommender.algorithm.AlgorithmConfiguration;
import de.dominikdassow.musicrs.recommender.engine.SimilarTracksEngine;
import de.dominikdassow.musicrs.service.DatabaseService;
import de.dominikdassow.musicrs.study.ExecuteAlgorithms;
import de.dominikdassow.musicrs.study.GenerateReferenceParetoFront;
import de.dominikdassow.musicrs.study.GenerateTrackIdsForBestParetoSets;
import lombok.extern.slf4j.Slf4j;
import org.uma.jmetal.lab.experiment.Experiment;
import org.uma.jmetal.lab.experiment.ExperimentBuilder;
import org.uma.jmetal.lab.experiment.component.impl.ComputeQualityIndicators;
import org.uma.jmetal.lab.experiment.component.impl.GenerateFriedmanTestTables;
import org.uma.jmetal.lab.experiment.component.impl.GenerateLatexTablesWithStatistics;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentProblem;
import org.uma.jmetal.lab.visualization.StudyVisualizer;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
public class ConductStudyTask
    extends Task {

    private SimilarTracksEngine similarTracksEngine;

    private Stream<Integer> playlists;

    private List<AlgorithmConfiguration<? extends Solution<Integer>>> algorithmConfigurations;

    public ConductStudyTask() {
        super("Conduct Study (" + AppConfiguration.get().studyName + ")");
    }

    public ConductStudyTask fromConfiguration() {
        JsonNode json = AppConfiguration.get().json;

        log.info(json.toPrettyString());

        JsonNode jsonPlaylists = json.get("studyPlaylists");

        if (jsonPlaylists.isNull()) {
            log.info("No playlists specified. Using all challenge playlists...");

            playlists = DatabaseService
                .readAllPlaylistChallenges()
                .sorted()
                .distinct();
        } else if (jsonPlaylists.isObject()) {
            log.info("Playlists range specified (" + jsonPlaylists + "). Using these challenge playlists...");

            playlists = DatabaseService
                .readAllPlaylistChallenges()
                .sorted()
                .distinct()
                .skip(jsonPlaylists.get("offset").asInt())
                .limit(jsonPlaylists.get("count").asInt());
        } else {
            playlists = StreamSupport.stream(jsonPlaylists.spliterator(), false)
                .map(JsonNode::asInt)
                .distinct();
        }

        algorithmConfigurations = StreamSupport.stream(json.get("studyAlgorithms").spliterator(), false)
            .distinct()
            .map(name -> AlgorithmConfiguration.fromName(name.asText()))
            .collect(Collectors.toList());

        return this;
    }

    public ConductStudyTask forPlaylists(Integer... playlist) {
        playlists = Set.of(playlist).stream();

        return this;
    }

    public ConductStudyTask using(List<AlgorithmConfiguration<? extends Solution<Integer>>> configurations) {
        algorithmConfigurations = configurations;

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

        playlists.parallel().forEach(playlist -> {
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

                IntStream.range(0, AppConfiguration.get().studyIndependentRuns).forEach(run -> {
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
            new ExperimentBuilder<Solution<Integer>, List<Solution<Integer>>>(AppConfiguration.get().studyName)
                .setAlgorithmList(algorithms)
                .setProblemList(new ArrayList<>(problems.values()))
                .setExperimentBaseDirectory(AppConfiguration.get().dataDirectory + "/study")
                .setOutputParetoFrontFileName("FUN")
                .setOutputParetoSetFileName("VAR")
                .setReferenceFrontDirectory(AppConfiguration.get().dataDirectory + "/study/"
                    + AppConfiguration.get().studyName + "/referenceFronts")
                .setIndicatorList(List.of(
                    // TODO: Check used indicators
                    // new NormalizedHypervolume<>(),
                    new PISAHypervolume<>(),
                    new Epsilon<>()
                ))
                .setIndependentRuns(AppConfiguration.get().studyIndependentRuns)
                .build();

        new ExecuteAlgorithms<>(experiment, AppConfiguration.get().studyMaxRetries).run();

        if (AppConfiguration.get().studyGenerateResults) {
            new GenerateReferenceParetoFront(experiment).run();
            new ComputeQualityIndicators<>(experiment).run();
            new GenerateTrackIdsForBestParetoSets(experiment).run();
            // TODO: Check study evaluations
            new GenerateFriedmanTestTables<>(experiment).run();
            new GenerateLatexTablesWithStatistics(experiment).run();
            new StudyVisualizer(AppConfiguration.get().dataDirectory + "/study/"
                + AppConfiguration.get().studyName, StudyVisualizer.TYPE_OF_FRONT_TO_SHOW.MEDIAN)
                .createHTMLPageForEachIndicator();
        }
    }

    private MusicPlaylistContinuationProblem.Configuration getProblemConfiguration(Integer playlist) {
        List<SimilarTracksList> similarTracksLists
            = DatabaseService.readSimilarTracksLists(playlist);

        long numberOfUniqueTracks = similarTracksLists.stream()
            .flatMap(similarTracksList -> similarTracksList.getTracks().stream())
            .distinct()
            .count();

        if (numberOfUniqueTracks < AppConfiguration.get().numberOfTracks) {
            log.warn("# [" + playlist + "] SIMILAR PLAYLISTS :: " +
                "Not enough unique tracks: " + numberOfUniqueTracks);

            return null;
        }

        log.info("# [" + playlist + "] SIMILAR PLAYLISTS :: " + numberOfUniqueTracks);

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
