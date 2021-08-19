package de.dominikdassow.musicrs.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dominikdassow.musicrs.model.Playlist;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.feature.FeatureGenerator;
import de.dominikdassow.musicrs.model.feature.TrackFeature;
import de.dominikdassow.musicrs.model.json.PlaylistJson;
import de.dominikdassow.musicrs.service.DatabaseService;
import de.dominikdassow.musicrs.service.database.ImportSession;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ImportDataTask
    extends Task {

    private ImportSession session;

    private Map<String, Map<TrackFeature.Audio, Double>> audioFeatures;

    public ImportDataTask() {
        super("Import Dataset");
    }

    @Override
    protected void init() throws Exception {
        session = new ImportSession();

        audioFeatures = DatabaseService.readTracksAudioFeatures();
        // audioFeatures = new HashMap<>();
    }

    @Override
    protected void execute() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // TODO: Remove counter?
        // TODO: Use database.allTrackIds here and filter directly after reading?
        // TODO: Currently not performing well at all when slicesPerBatch > 5 (only w/ audio features)
        // TODO: Parallel stream are not better right now. Why?

        final int slices = 100; // TODO
        final int slicesOffset = 0;
        final int slicesPerBatch = 5; // TODO
        final int sliceSize = 1_000;

        final List<Playlist> batchPlaylists = new ArrayList<>();
        final Map<String, Track> batchTracks = new HashMap<>();

        final int offset = slicesOffset * sliceSize;

        for (int i = offset; i < (offset + (slices * sliceSize)); i += sliceSize) {
            final String slice = i + "-" + (i + sliceSize - 1);

            log.info("> SLICE: " + slice);

            JsonNode root = objectMapper
                .readTree(new File("data/mpd/mpd.slice." + slice + ".json"));

            process(objectMapper.treeToValue(root.get("playlists"), PlaylistJson[].class))
                .forEach(playlist -> {
                    batchPlaylists.add(playlist);
                    playlist.getTracks().values()
                        .forEach(track -> batchTracks.putIfAbsent(track.getId(), track));
                });

            final boolean batchSizeReached = ((i + sliceSize) % (slicesPerBatch * sliceSize)) == 0;
            final boolean isLast = (i + sliceSize) == (offset + (slices * sliceSize));

            if (batchSizeReached || isLast) {
                session.storePlaylists(batchPlaylists);
                session.storeTracks(batchTracks);

                batchPlaylists.clear();
                batchTracks.clear();
            }
        }

        JsonNode root = objectMapper
            .readTree(new File("data/challenge/challenge_set.json"));

        session.storePlaylists(process(objectMapper.treeToValue(root.get("playlists"), PlaylistJson[].class))
            .collect(Collectors.toList()));
    }

    @Override
    protected void finish() {
        session.close();

        log.info("PLAYLIST :: EXPECT=" + session.playlistStoreCount.get());
        log.info("PLAYLIST :: HAS=" + DatabaseService.readStore(DatabaseService.Store.PLAYLIST).count());

        log.info("PLAYLIST_FEATURE :: EXPECT=" + session.playlistFeatureStoreCount.get());
        log.info("PLAYLIST_FEATURE :: HAS=" + DatabaseService.readStore(DatabaseService.Store.PLAYLIST_FEATURE).count());

        log.info("TRACK :: EXPECT=" + session.trackStoreCount.get());
        log.info("TRACK :: HAS=" + DatabaseService.readStore(DatabaseService.Store.TRACK).count());

        log.info("TRACK_FEATURE :: EXPECT=" + session.trackFeatureStoreCount.get());
        log.info("TRACK_FEATURE :: HAS=" + DatabaseService.readStore(DatabaseService.Store.TRACK_FEATURE).count());
    }

    private Stream<Playlist> process(PlaylistJson[] json) {
        return Arrays.stream(json)
            .map(PlaylistJson::asPlaylist)
            .peek(playlist -> playlist.getTracks().values().forEach(track -> {
                track.setAudioFeaturesFrom(audioFeatures);
                track.setFeatures(FeatureGenerator.generateFor(track));
            }))
            .map(playlist -> playlist.toBuilder()
                .features(FeatureGenerator.generateFor(playlist))
                .build());
    }
}
