package de.dominikdassow.musicrs;

import de.dominikdassow.musicrs.service.DatabaseService;
import de.dominikdassow.musicrs.service.RecommendationService;
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
    private DatabaseService database;

    @Autowired
    private RecommendationService recommendations;

    public static void main(String[] args) {
        new SpringApplicationBuilder(MusicRsApplication.class)
            .bannerMode(Banner.Mode.OFF)
            .web(WebApplicationType.NONE)
            .logStartupInfo(false)
            .run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("MusicRsApplication: " + Arrays.toString(args));

        database.init();
        recommendations.run();
    }
}
