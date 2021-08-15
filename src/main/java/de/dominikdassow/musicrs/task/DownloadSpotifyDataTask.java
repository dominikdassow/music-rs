package de.dominikdassow.musicrs.task;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Iterators;
import com.google.common.collect.Streams;
import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.service.DatabaseService;
import de.dominikdassow.musicrs.service.SpotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
@Component
@SuppressWarnings("unused")
public class DownloadSpotifyDataTask
    extends Task {

    @Autowired
    private SpotifyService spotify;

    @Autowired
    private DatabaseService database;

    private boolean onlyMissing = false;

    public DownloadSpotifyDataTask() {
        super("Download Spotify Data");
    }

    public DownloadSpotifyDataTask onlyMissing(boolean onlyMissing) {
        this.onlyMissing = onlyMissing;

        return this;
    }

    @Override
    protected void init() {
        spotify.init();
    }

    @Override
    protected void execute() throws Exception {
        if (onlyMissing) {
            downloadMissing();
        } else {
            downloadAll();
        }
    }

    private void downloadAll() throws Exception {
        PrintWriter writer
            = new PrintWriter(new FileWriter("data/spotify/spotify-tracks-data.csv"));

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

        final int count
            = downloadAudioFeatures(writer, database.streamAllTracksWithIdAndUri(100));

        log.info("> COUNT: " + count);

        writer.close();
    }

    private void downloadMissing() throws Exception {
        Set<String> existingUris = new HashSet<>();

        try (Stream<String> stream = Files.lines(Paths.get("data/spotify/spotify-tracks-data.csv"))) {
            stream.skip(1)
                .map(data -> data.split(",")[0])
                .forEach(existingUris::add);
        }

        log.info("EXISTING: " + existingUris.size());

        Set<Track.WithIdAndUri> missingTracks = new HashSet<>();

        database.streamAllTracksWithIdAndUri().parallel()
            .filter(track -> !existingUris.contains(track.getUri()))
            .forEach(missingTracks::add);

        log.info("MISSING: " + missingTracks.size());

        PrintWriter writer
            = new PrintWriter(new FileWriter("data/spotify/spotify-tracks-data.csv", true));

        final int count
            = downloadAudioFeatures(writer, Streams.stream(Iterators.partition(missingTracks.iterator(), 100)));

        log.info("> STILL MISSING: " + (missingTracks.size() - count));

        writer.close();
    }

    private int downloadAudioFeatures(PrintWriter writer, Stream<List<Track.WithIdAndUri>> batches) {
        AtomicInteger batchIndex = new AtomicInteger();
        AtomicInteger counter = new AtomicInteger();

        batches.parallel().forEach(tracks -> {
            log.trace("Audio Features :: " + batchIndex.getAndIncrement());

            final AudioFeatures[] audioFeatures;

            try {
                audioFeatures = spotify.getAudioFeatures(tracks);
            } catch (Exception e) {
                log.error(e.getMessage(), e);

                return;
            }

            Arrays.stream(audioFeatures)
                .filter(Objects::nonNull)
                .forEach(data -> {
                    counter.incrementAndGet();
                    writer.println(format(data));
                    writer.flush();
                });
        });

        return counter.get();
    }

    private static String format(AudioFeatures audioFeatures) {
        return String.join(",",
            audioFeatures.getUri(),
            format(audioFeatures.getAcousticness()),
            format(audioFeatures.getDanceability()),
            format(audioFeatures.getEnergy()),
            format(audioFeatures.getInstrumentalness()),
            audioFeatures.getKey().toString(),
            format(audioFeatures.getLiveness()),
            format(audioFeatures.getLoudness()),
            String.valueOf(audioFeatures.getMode().mode),
            format(audioFeatures.getSpeechiness()),
            format(audioFeatures.getTempo()),
            audioFeatures.getTimeSignature().toString(),
            format(audioFeatures.getValence())
        );
    }

    private static String format(Float value) {
        final String s = String.format(Locale.US, "%.4f", value);

        if (s.equals("0.0000")) return "0";

        String trimmed = CharMatcher.anyOf("0").trimTrailingFrom(s);

        if (trimmed.charAt(trimmed.length() - 1) == '.') trimmed += "0";

        return trimmed;
    }
}
