package de.dominikdassow.musicrs;

import de.dominikdassow.musicrs.recommender.algorithm.AlgorithmConfiguration;
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

    public static void main(String[] args) {
        log.info("MusicRsApplication: " + Arrays.toString(args));

        final Task.Type task = Task.Type.EVALUATE_SAMPLES;

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
                        AlgorithmConfiguration.NSGAII.builder()
                            .populationSize(100).maxEvaluations(5_000)
                            .crossoverProbability(0.9).mutationProbability(0.2)
                            .build()
                    )
                    .run();
                break;
            case EVALUATE_SAMPLES:
                evaluateSamplesTask
                    .sampling(483, 777)
                    .using(
                        AlgorithmConfiguration.NSGAII.builder()
                            .populationSize(100).maxEvaluations(5_000)
                            .crossoverProbability(0.9).mutationProbability(0.2)
                            .build(),
                        AlgorithmConfiguration.NSGAII.builder()
                            .populationSize(100).maxEvaluations(5_000)
                            .crossoverProbability(0.8).mutationProbability(0.3)
                            .build()
                    )
                    .run();
                break;
        }
    }
}
