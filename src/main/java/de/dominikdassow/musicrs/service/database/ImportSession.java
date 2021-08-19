package de.dominikdassow.musicrs.service.database;

import de.dominikdassow.musicrs.model.Playlist;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.service.DatabaseService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ImportSession {

    public final PrintWriter playlistStore;
    public final PrintWriter playlistFeatureStore;
    public final PrintWriter trackStore;
    public final PrintWriter trackFeatureStore;

    public final AtomicInteger playlistStoreCount = new AtomicInteger();
    public final AtomicInteger playlistFeatureStoreCount = new AtomicInteger();
    public final AtomicInteger trackStoreCount = new AtomicInteger();
    public final AtomicInteger trackFeatureStoreCount = new AtomicInteger();

    private final Set<String> allTrackIds = new HashSet<>();

    public ImportSession() throws IOException {
        playlistStore = DatabaseService.newStore(DatabaseService.Store.PLAYLIST);
        playlistFeatureStore = DatabaseService.newStore(DatabaseService.Store.PLAYLIST_FEATURE);
        trackStore = DatabaseService.newStore(DatabaseService.Store.TRACK);
        trackFeatureStore = DatabaseService.newStore(DatabaseService.Store.TRACK_FEATURE);
    }

    public void close() {
        playlistStore.close();
        playlistFeatureStore.close();
        trackStore.close();
        trackFeatureStore.close();
    }

    public void storePlaylists(List<Playlist> playlists) {
        playlistStoreCount.addAndGet(playlists.size());
        playlistFeatureStoreCount.addAndGet(playlists.stream()
            .flatMapToInt(playlist -> IntStream.of(playlist.getFeatures().size()))
            .sum());

        playlists.forEach(playlist -> {
            // Ensure track positions by storing empty strings for non-existing positions.
            // Important for for challenge playlists with randomly missing tacks.
            final String tracks = IntStream
                .range(0, playlist.getTracks().size())
                .mapToObj(i -> playlist.getTracks().containsKey(i) ? playlist.getTracks().get(i).getId() : "")
                .collect(Collectors.joining(DatabaseService.DELIMITER));

            playlistStore.println(playlist.getId()
                + DatabaseService.DELIMITER + tracks);

            playlist.getFeatures().forEach(playlistFeature -> playlistFeatureStore.println(playlist.getId()
                + DatabaseService.DELIMITER + playlistFeature.getId()
                + DatabaseService.DELIMITER + playlistFeature.formattedValue()));
        });

        playlistStore.flush();
        playlistFeatureStore.flush();
    }

    public void storeTracks(Map<String, Track> tracks) {
        trackStoreCount.addAndGet((int) tracks.values().stream()
            .filter(track -> !allTrackIds.contains(track.getId())).count());

        trackFeatureStoreCount.addAndGet(tracks.values().stream()
            .filter(track -> !allTrackIds.contains(track.getId()))
            .flatMapToInt(track -> IntStream.of(track.getFeatures().size()))
            .sum());

        tracks.entrySet().stream().sequential().forEach(entry -> {
            if (allTrackIds.contains(entry.getKey())) return;

            allTrackIds.add(entry.getKey());

            trackStore.println(entry.getKey()
                + DatabaseService.DELIMITER + entry.getValue().getArtistId()
                + DatabaseService.DELIMITER + entry.getValue().getAlbumId());

            entry.getValue().getFeatures().forEach(trackFeature -> trackFeatureStore.println(entry.getKey()
                + DatabaseService.DELIMITER + trackFeature.getId()
                + DatabaseService.DELIMITER + trackFeature.getValue()));
        });

        trackStore.flush();
        trackFeatureStore.flush();
    }
}
