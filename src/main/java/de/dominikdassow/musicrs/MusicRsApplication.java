package de.dominikdassow.musicrs;

import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Arrays;

@SpringBootApplication
public class MusicRsApplication
    implements CommandLineRunner {

    public static void main(String[] args) {
        new SpringApplicationBuilder(MusicRsApplication.class)
            .bannerMode(Banner.Mode.OFF)
            .web(WebApplicationType.NONE)
            .logStartupInfo(false)
            .run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Hello World");
        System.out.println(Arrays.toString(args));
    }
}
