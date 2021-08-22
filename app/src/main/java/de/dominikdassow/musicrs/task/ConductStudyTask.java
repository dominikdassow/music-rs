package de.dominikdassow.musicrs.task;

import de.dominikdassow.musicrs.model.SimilarTracksList;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationAlgorithm;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationProblem;
import de.dominikdassow.musicrs.recommender.algorithm.AlgorithmConfiguration;
import de.dominikdassow.musicrs.recommender.engine.SimilarPlaylistsEngine;
import de.dominikdassow.musicrs.recommender.engine.SimilarTracksEngine;
import de.dominikdassow.musicrs.service.DatabaseService;
import de.dominikdassow.musicrs.study.GenerateTrackIdsForBestParetoSets;
import lombok.extern.slf4j.Slf4j;
import org.uma.jmetal.lab.experiment.Experiment;
import org.uma.jmetal.lab.experiment.ExperimentBuilder;
import org.uma.jmetal.lab.experiment.component.impl.ComputeQualityIndicators;
import org.uma.jmetal.lab.experiment.component.impl.ExecuteAlgorithms;
import org.uma.jmetal.lab.experiment.component.impl.GenerateReferenceParetoFront;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentProblem;
import org.uma.jmetal.qualityindicator.impl.Epsilon;
import org.uma.jmetal.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

@Slf4j
public class ConductStudyTask
    extends Task {

    private static final String STUDY_NAME = "MusicPlaylistContinuationStudy";
    private static final String STUDY_DIRECTORY = "../data/study";

    private static final int INDEPENDENT_RUNS = 2; // TODO

    private SimilarPlaylistsEngine similarPlaylistsEngine;
    private SimilarTracksEngine similarTracksEngine;

    private List<AlgorithmConfiguration> algorithmConfigurations;

    private Set<Integer> playlists;

    public ConductStudyTask() {
        super("Conduct Study");
    }

    public ConductStudyTask forPlaylists(Integer... playlist) {
        this.playlists = Set.of(playlist);

        return this;
    }

    public ConductStudyTask using(AlgorithmConfiguration... algorithmConfigurations) {
        this.algorithmConfigurations = List.of(algorithmConfigurations);

        return this;
    }

    @Override
    protected void init() {
        similarPlaylistsEngine = new SimilarPlaylistsEngine();
        similarTracksEngine = new SimilarTracksEngine();
    }

    @Override
    protected void execute() throws IOException {
        List<ExperimentProblem<PermutationSolution<Integer>>> problems
            = getProblems();

        List<ExperimentAlgorithm<PermutationSolution<Integer>, List<PermutationSolution<Integer>>>> algorithms
            = getAlgorithmsFor(problems);

        Experiment<PermutationSolution<Integer>, List<PermutationSolution<Integer>>> experiment =
            new ExperimentBuilder<PermutationSolution<Integer>, List<PermutationSolution<Integer>>>(STUDY_NAME)
                .setAlgorithmList(algorithms)
                .setProblemList(problems)
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
                .setNumberOfCores(2) // TODO
                .build();

        new ExecuteAlgorithms<>(experiment).run();
        new GenerateReferenceParetoFront(experiment).run();
        new ComputeQualityIndicators<>(experiment).run();
        new GenerateTrackIdsForBestParetoSets(experiment).run();
        // TODO: Check study evaluations
//        new GenerateFriedmanTestTables<>(experiment).run();
//        new GenerateLatexTablesWithStatistics(experiment).run();
//        new StudyVisualizer(STUDY_DIRECTORY + "/" + STUDY_NAME, StudyVisualizer.TYPE_OF_FRONT_TO_SHOW.MEDIAN)
//            .createHTMLPageForEachIndicator();
    }

    private List<ExperimentProblem<PermutationSolution<Integer>>>
    getProblems() {
        return new ArrayList<>() {{
            playlists.forEach(playlist -> {
                // TODO: Generate similar playlists in its own task -> Only read similar playlists list here
                final List<SimilarTracksList> similarTracksLists
                    = similarPlaylistsEngine.getSimilarTracksFor(playlist, 500); // TODO: Constant

                log.info("# [" + playlist + "] SIMILAR PLAYLISTS: "
                    + similarTracksLists.size() + " :: "
                    + similarTracksLists.stream()
                    .flatMap(similarTracksList -> similarTracksList.getTracks().stream())
                    .distinct()
                    .count());

                Map<Integer, String> tracks = DatabaseService.readPlaylistTracks(playlist);

                final ExperimentProblem<PermutationSolution<Integer>> problem = new ExperimentProblem<>(
                    new MusicPlaylistContinuationProblem(similarTracksEngine, tracks, similarTracksLists),
                    "MPC_" + playlist
                ).setReferenceFront("MPC_" + playlist + ".csv");

                add(problem);
            });
        }};
    }

    private List<ExperimentAlgorithm<PermutationSolution<Integer>, List<PermutationSolution<Integer>>>>
    getAlgorithmsFor(List<ExperimentProblem<PermutationSolution<Integer>>> problems) {
        return new ArrayList<>() {{
            problems.forEach(problem -> algorithmConfigurations.forEach(configuration -> {
                final MusicPlaylistContinuationAlgorithm algorithm
                    = configuration.createAlgorithmFor((MusicPlaylistContinuationProblem) problem.getProblem());

                IntStream.range(0, INDEPENDENT_RUNS).forEach(run -> add(new ExperimentAlgorithm<>(
                    algorithm.get(),
                    configuration.getName(),
                    problem,
                    run
                )));
            }));
        }};
    }
}
