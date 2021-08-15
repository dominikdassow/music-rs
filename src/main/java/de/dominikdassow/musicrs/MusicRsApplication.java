package de.dominikdassow.musicrs;

import de.dominikdassow.musicrs.task.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Arrays;

@SpringBootApplication
@Slf4j
@SuppressWarnings("unused")
public class MusicRsApplication
    implements CommandLineRunner {

    @Autowired
    private ImportDatasetTask importDatasetTask;

    @Autowired
    private ImportChallengeSetTask importChallengeSetTask;

    @Autowired
    private DownloadSpotifyDataTask downloadSpotifyDataTask;

    @Autowired
    private MakeRecommendationTask makeRecommendationTask;

    @Autowired
    private EvaluateSamplePlaylistsTask evaluateSamplePlaylistsTask;

    public static void main(String[] args) {
        new SpringApplicationBuilder(MusicRsApplication.class)
            .bannerMode(Banner.Mode.OFF)
            .web(WebApplicationType.NONE)
            .logStartupInfo(false)
            .lazyInitialization(true)
            .run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("MusicRsApplication: " + Arrays.toString(args));

//        importDatasetTask
//            .rebuilding(false)
//            .run();

//        importChallengeSetTask
//            .rebuilding(false)
//            .run();

//        downloadSpotifyDataTask
//            .onlyMissing(true)
//            .run();

//        makeRecommendationTask
//            .forChallengePlaylists(1_000_800)
//            .using(RecommendationService.AlgorithmType.NSGAII)
//            .run();

//        evaluateSamplePlaylistsTask
//            .using(RecommendationService.AlgorithmType.NSGAII)
//            .sampling(90)
//            .run();
    }
}
