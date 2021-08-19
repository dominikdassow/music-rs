package de.dominikdassow.musicrs.task;

import com.google.common.collect.Iterators;
import com.google.common.collect.Streams;
import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import de.dominikdassow.musicrs.service.DatabaseService;
import de.dominikdassow.musicrs.service.SpotifyService;
import de.dominikdassow.musicrs.service.database.DownloadSpotifyDataSession;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
public class DownloadSpotifyDataTask
    extends Task {

    private final List<String> existingTracks = new ArrayList<>();

    private DownloadSpotifyDataSession session;
    private SpotifyService spotify;

    public DownloadSpotifyDataTask() {
        super("Download Spotify Data");
    }

    public DownloadSpotifyDataTask onlyMissing(boolean onlyMissing) {
        if (!onlyMissing) return this;

        DatabaseService.readStore(DatabaseService.Store.TRACK_AUDIO_FEATURE)
            .map(data -> data.split(DatabaseService.DELIMITER)[0])
            .forEach(existingTracks::add);

        log.info("EXISTING TRACKS :: " + existingTracks.size());

        return this;
    }

    @Override
    protected void init() throws Exception {
        session = new DownloadSpotifyDataSession(existingTracks.isEmpty());
        spotify = new SpotifyService();
    }

    @Override
    protected void execute() {
        Stream<String> tracks = DatabaseService
            .readStore(DatabaseService.Store.TRACK)
            .map(data -> data.split(DatabaseService.DELIMITER)[0])
            .filter(data -> !existingTracks.contains(data));

        download(tracks);
    }

    @Override
    protected void finish() {
        session.close();

        if (!existingTracks.isEmpty())
            log.info("TRACK_AUDIO_FEATURE :: EXISTING=" + existingTracks.size());

        log.info("TRACK_AUDIO_FEATURE :: DOWNLOADED=" + session.trackAudioFeatureStoreCount.get());
        log.info("TRACK_AUDIO_FEATURE :: HAS=" + DatabaseService.readStore(DatabaseService.Store.TRACK_AUDIO_FEATURE).count());
    }

    private void download(Stream<String> tracks) {
        AtomicInteger batch = new AtomicInteger();

        Streams.stream(Iterators.partition(tracks.iterator(), 100)).parallel().forEach(ids -> {
            log.info("Audio Features :: " + batch.getAndIncrement());

            final AudioFeatures[] audioFeatures;

            try {
                audioFeatures = spotify.getAudioFeatures(ids);
            } catch (Exception e) {
                log.error(e.getMessage(), e);

                return;
            }

            session.storeTrackAudioFeatures(audioFeatures);
        });
    }
}
