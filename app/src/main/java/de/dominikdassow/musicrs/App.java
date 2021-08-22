package de.dominikdassow.musicrs;

import de.dominikdassow.musicrs.recommender.algorithm.NSGAII;
import de.dominikdassow.musicrs.task.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class App {

    private static final DownloadSpotifyDataTask downloadSpotifyDataTask
        = new DownloadSpotifyDataTask();

    private static final ImportDataTask importDataTask
        = new ImportDataTask();

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
            case MAKE_RECOMMENDATIONS:
                makeRecommendations
                    .forPlaylists(1_000_800)
                    .using(
                        NSGAII.Configuration.builder()
                            .populationSize(100).maxEvaluations(5_000)
                            .crossoverProbability(0.9).mutationProbability(0.2)
                            .build()
                    )
                    .run();
                break;
            case EVALUATE_SAMPLES:
                evaluateSamplesTask
                    .sampling(781)
                    .using(
                        NSGAII.Configuration.builder()
                            .populationSize(100).maxEvaluations(5_000)
                            .crossoverProbability(0.9).mutationProbability(0.2)
                            .build()
                    )
                    .run();
                break;
            case CONDUCT_STUDY:
                conductStudyTask
                    .forPlaylists(1_003_178, 1_006_778) // 1_003_178, 1_003_183, 1_026_297, 1_034_933, 1_006_778
                    .using(
                        NSGAII.Configuration.builder()
                            .populationSize(100).maxEvaluations(5_000)
                            .crossoverProbability(0.7).mutationProbability(0.1)
                            .build(),
                        NSGAII.Configuration.builder()
                            .populationSize(100).maxEvaluations(5_000)
                            .crossoverProbability(0.7).mutationProbability(0.2)
                            .build(),
                        NSGAII.Configuration.builder()
                            .populationSize(100).maxEvaluations(5_000)
                            .crossoverProbability(0.7).mutationProbability(0.3)
                            .build()
                    )
                    .run();
                break;
        }
    }
}
