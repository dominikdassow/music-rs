package de.dominikdassow.musicrs;

import de.dominikdassow.musicrs.recommender.algorithm.MACO;
import de.dominikdassow.musicrs.recommender.algorithm.NOOP;
import de.dominikdassow.musicrs.recommender.algorithm.NSGAII;
import de.dominikdassow.musicrs.recommender.algorithm.SMSEMOA;
import de.dominikdassow.musicrs.task.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class App {

    private static final DownloadSpotifyDataTask downloadSpotifyDataTask
        = new DownloadSpotifyDataTask();

    private static final ImportDataTask importDataTask
        = new ImportDataTask();

    private static final GenerateSimilarTracksListsTask generateSimilarTracksListsTask
        = new GenerateSimilarTracksListsTask();

    private static final MakeRecommendationsTask makeRecommendations
        = new MakeRecommendationsTask();

    private static final EvaluateSamplesTask evaluateSamplesTask
        = new EvaluateSamplesTask();

    private static final ConductStudyTask conductStudyTask
        = new ConductStudyTask();

    public static void main(String[] args) {
        log.info("MusicRsApplication: " + Arrays.toString(args));

        final Task.Type task = Task.Type.CONDUCT_STUDY;

        switch (task) {
            case DOWNLOAD_SPOTIFY_DATA:
                downloadSpotifyDataTask
                    .onlyMissing(false)
                    .run();
                break;
            case IMPORT_DATA:
                importDataTask
                    .run();
                break;
            case GENERATE_SIMILAR_TRACKS_LISTS:
                generateSimilarTracksListsTask
                    .run();
                break;
            case MAKE_RECOMMENDATIONS:
                makeRecommendations
                    .forPlaylists(1_000_069)
                    .using(List.of(
                        MACO.Configuration.builder()
                            .variant(MACO.Configuration.Variant.MULTIPLE_COLONIES_MULTIPLE_RANDOM_PHEROMONE_TRAILS)
                            .numberOfAnts(2).numberOfCycles(2)
                            .pheromoneFactorsWeight(1.0).heuristicFactorsWeight(1.0).evaporationFactor(0.8)
                            .build()
                    ))
                    .run();
                break;
            case EVALUATE_SAMPLES:
                evaluateSamplesTask
                    .sampling(90)
                    .using(List.of(
//                        NSGAII.Configuration.builder()
//                            .populationSize(100).maxEvaluations(5_000)
//                            .crossoverProbability(0.7).mutationProbability(0.2)
//                            .build(),
//                        MACO.Configuration.builder()
//                            .variant(MACO.Configuration.Variant.ONE_COLONY_MULTIPLE_RANDOM_PHEROMONE_TRAILS)
//                            .numberOfAnts(5).numberOfCycles(100)
//                            .pheromoneFactorsWeight(0.5).heuristicFactorsWeight(0.5).evaporationFactor(0.8)
//                            .build(),
                        MACO.Configuration.builder()
                            .variant(MACO.Configuration.Variant.MULTIPLE_COLONIES_MULTIPLE_RANDOM_PHEROMONE_TRAILS)
                            .numberOfAnts(5).numberOfCycles(50)
                            .pheromoneFactorsWeight(0.25).heuristicFactorsWeight(0.75).evaporationFactor(0.2)
                            .build()
//                        MACO.Configuration.builder()
//                            .variant(MACO.Configuration.Variant.MULTIPLE_COLONIES_MULTIPLE_SUMMED_PHEROMONE_TRAILS)
//                            .numberOfAnts(10).numberOfCycles(3)
//                            .pheromoneFactorsWeight(0.5).heuristicFactorsWeight(0.5).evaporationFactor(0.8)
//                            .build(),
//                        MACO.Configuration.builder()
//                            .variant(MACO.Configuration.Variant.ONE_COLONY_ONE_PHEROMONE_TRAIL)
//                            .numberOfAnts(10).numberOfCycles(3)
//                            .pheromoneFactorsWeight(0.5).heuristicFactorsWeight(0.5).evaporationFactor(0.8)
//                            .build(),
//                        MACO.Configuration.builder()
//                            .variant(MACO.Configuration.Variant.ONE_COLONY_MULTIPLE_RANDOM_PHEROMONE_TRAILS)
//                            .numberOfAnts(10).numberOfCycles(3)
//                            .pheromoneFactorsWeight(0.5).heuristicFactorsWeight(0.5).evaporationFactor(0.8)
//                            .build()
                    ))
                    .run();
                break;
            case CONDUCT_STUDY:
                conductStudyTask
                    // 0, 1, 5, 10, 25, 100 (number of tracks)
                    // .forPlaylists(1_000_069, 1_003_178, 1_006_778, 1_008_706, 1002349, 1020917)
                    .forPlaylists(1_006_778, 1_008_706)
                    .using(List.of(
                        NOOP.Configuration.builder()
                            .populationSize(100)
                            .build(),
//                        NSGAII.Configuration.builder()
//                            .populationSize(100).maxEvaluations(5_000)
//                            .crossoverProbability(0.7).mutationProbability(0.1)
//                            .build(),
                        NSGAII.Configuration.builder()
                            .populationSize(100).maxEvaluations(5_000)
                            .crossoverProbability(0.7).mutationProbability(0.2)
                            .build(),
//                        NSGAII.Configuration.builder()
//                            .populationSize(100).maxEvaluations(5_000)
//                            .crossoverProbability(0.7).mutationProbability(0.3)
//                            .build(),
//                        SMSEMOA.Configuration.builder()
//                            .populationSize(100).maxEvaluations(5_000)
//                            .crossoverProbability(0.7).mutationProbability(0.1)
//                            .build(),
//                        SMSEMOA.Configuration.builder()
//                            .populationSize(100).maxEvaluations(5_000)
//                            .crossoverProbability(0.7).mutationProbability(0.2)
//                            .build(),
//                        SMSEMOA.Configuration.builder()
//                            .populationSize(100).maxEvaluations(5_000)
//                            .crossoverProbability(0.7).mutationProbability(0.3)
//                            .build(),
//                        MACO.Configuration.builder()
//                            .variant(MACO.Configuration.Variant.MULTIPLE_COLONIES_MULTIPLE_RANDOM_PHEROMONE_TRAILS)
//                            .numberOfAnts(6).numberOfCycles(100)
//                            .pheromoneFactorsWeight(1.0).heuristicFactorsWeight(4.0).evaporationFactor(0.1)
//                            .build(),
                        MACO.Configuration.builder()
                            .variant(MACO.Configuration.Variant.MULTIPLE_COLONIES_MULTIPLE_SUMMED_PHEROMONE_TRAILS)
                            .numberOfAnts(5).numberOfCycles(20)
                            .pheromoneFactorsWeight(1.0).heuristicFactorsWeight(4.0).evaporationFactor(0.1)
                            .build()
//                        MACO.Configuration.builder()
//                            .variant(MACO.Configuration.Variant.ONE_COLONY_ONE_PHEROMONE_TRAIL)
//                            .numberOfAnts(2).numberOfCycles(300)
//                            .pheromoneFactorsWeight(1.0).heuristicFactorsWeight(8.0).evaporationFactor(0.01)
//                            .build(),
//                        MACO.Configuration.builder()
//                            .variant(MACO.Configuration.Variant.ONE_COLONY_MULTIPLE_RANDOM_PHEROMONE_TRAILS)
//                            .numberOfAnts(100).numberOfCycles(3000)
//                            .pheromoneFactorsWeight(1.0).heuristicFactorsWeight(4.0).evaporationFactor(0.01)
//                            .build()
                    ))
                    .run();
                break;
        }
    }
}
