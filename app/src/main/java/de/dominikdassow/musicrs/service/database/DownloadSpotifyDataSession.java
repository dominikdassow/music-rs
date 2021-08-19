package de.dominikdassow.musicrs.service.database;

import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import de.dominikdassow.musicrs.service.DatabaseService;
import de.dominikdassow.musicrs.util.Formatter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadSpotifyDataSession {

    public final PrintWriter trackAudioFeatureStore;

    public final AtomicInteger trackAudioFeatureStoreCount = new AtomicInteger();

    public DownloadSpotifyDataSession(boolean createNewStore) throws IOException {
        if (createNewStore) {
            trackAudioFeatureStore = DatabaseService.newStore(DatabaseService.Store.TRACK_AUDIO_FEATURE);
        } else {
            trackAudioFeatureStore = DatabaseService.openStore(DatabaseService.Store.TRACK_AUDIO_FEATURE);
        }
    }

    public void close() {
        trackAudioFeatureStore.close();
    }

    public void storeTrackAudioFeatures(AudioFeatures[] audioFeatures) {
        Arrays.stream(audioFeatures)
            .filter(Objects::nonNull)
            .forEach(data -> {
                trackAudioFeatureStoreCount.incrementAndGet();
                trackAudioFeatureStore.println(String.join(DatabaseService.DELIMITER,
                    data.getId(),
                    Formatter.format(data.getAcousticness()),
                    Formatter.format(data.getDanceability()),
                    Formatter.format(data.getEnergy()),
                    Formatter.format(data.getInstrumentalness()),
                    data.getKey().toString(),
                    Formatter.format(data.getLiveness()),
                    Formatter.format(data.getLoudness()),
                    String.valueOf(data.getMode().mode),
                    Formatter.format(data.getSpeechiness()),
                    Formatter.format(data.getTempo()),
                    data.getTimeSignature().toString(),
                    Formatter.format(data.getValence())
                ));
            });

        trackAudioFeatureStore.flush();
    }
}
