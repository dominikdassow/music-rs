package de.dominikdassow.musicrs.task;

import com.google.common.base.CharMatcher;
import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import de.dominikdassow.musicrs.service.DatabaseService;
import de.dominikdassow.musicrs.service.SpotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@SuppressWarnings("unused")
public class DownloadSpotifyDataTask
    extends Task {

    @Autowired
    private SpotifyService spotify;

    @Autowired
    private DatabaseService database;

    public DownloadSpotifyDataTask() {
        super("Download Spotify Data");
    }

    @Override
    protected void init() {
        spotify.init();
    }

    @Override
    protected void execute() throws Exception {
        PrintWriter writer
            = new PrintWriter(new FileWriter("data/spotify/spotify-tracks-data.csv"));
        PrintWriter errorWriter
            = new PrintWriter(new FileWriter("data/spotify/spotify-tracks-data-errors.txt"));

        writer.println(String.join(",",
            "track_uri",
            "acousticness",
            "danceability",
            "energy",
            "instrumentalness",
            "key",
            "liveness",
            "loudness",
            "mode",
            "speechiness",
            "tempo",
            "time_signature",
            "valence"
        ));
        writer.flush();

        AtomicInteger counter = new AtomicInteger();

        database.streamAllTracksWithIdAndUri(100).parallel().forEach(tracks -> {
            log.trace("GetAudioFeatures :: " + counter.getAndIncrement());

            final AudioFeatures[] audioFeatures;

            try {
                audioFeatures = spotify.getAudioFeatures(tracks);
            } catch (Exception e) {
                log.error(e.getMessage());

                errorWriter.println("READ | " + String.join(", ", spotify.extractTrackIds(tracks)));
                errorWriter.flush();

                return;
            }

            try {
                Arrays.stream(audioFeatures).forEach(audioFeature -> {
                    writer.println(String.join(",",
                        audioFeature.getUri(),
                        format(audioFeature.getAcousticness()),
                        format(audioFeature.getDanceability()),
                        format(audioFeature.getEnergy()),
                        format(audioFeature.getInstrumentalness()),
                        audioFeature.getKey().toString(),
                        format(audioFeature.getLiveness()),
                        format(audioFeature.getLoudness()),
                        String.valueOf(audioFeature.getMode().mode),
                        format(audioFeature.getSpeechiness()),
                        format(audioFeature.getTempo()),
                        audioFeature.getTimeSignature().toString(),
                        format(audioFeature.getValence())
                    ));

                    writer.flush();
                });
            } catch (Exception e) {
                log.error(e.getMessage());

                errorWriter.println("EXTRACT | " + String.join(", ", spotify.extractTrackIds(tracks)));
                errorWriter.flush();
            }
        });

        writer.close();
        errorWriter.close();
    }

    private static String format(Float value) {
        final String s = String.format(Locale.US, "%.4f", value);

        if (s.equals("0.0000")) return "0";

        return CharMatcher.anyOf("0").trimTrailingFrom(s);
    }
}
