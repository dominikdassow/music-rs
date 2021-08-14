package de.dominikdassow.musicrs.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.playlist.DatasetPlaylist;
import de.dominikdassow.musicrs.service.DatabaseService;
import de.dominikdassow.musicrs.service.database.TrackIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@SuppressWarnings("unused")
public class ImportDatasetTask
    extends Task {

    @Autowired
    private DatabaseService database;

    private boolean rebuild = false;

    public ImportDatasetTask() {
        super("Import Dataset");
    }

    public ImportDatasetTask rebuilding(boolean rebuild) {
        this.rebuild = rebuild;

        return this;
    }

    @Override
    protected void init() {
        if (rebuild) database.resetDataset();

        TrackIdGenerator.init(database.findAllTracksWithIdAndUri());
    }

    @Override
    protected void execute() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        final int slices = 1_000;
        final int slicesOffset = 0;
        final int slicesPerBatch = 25;
        final int sliceSize = 1_000;

        final Map<Integer, DatasetPlaylist> batchPlaylists = new HashMap<>();
        final Map<Integer, Track> batchTracks = new HashMap<>();

        final int offset = slicesOffset * sliceSize;

        for (int i = offset; i < (offset + (slices * sliceSize)); i += sliceSize) {
            final String slice = i + "-" + (i + sliceSize - 1);

            log.info("> SLICE: " + slice);

            JsonNode root = objectMapper
                .readTree(new File("data/mpd/mpd.slice." + slice + ".json"));

            List<DatasetPlaylist> playlists
                = Arrays.asList(objectMapper.treeToValue(root.get("playlists"), DatasetPlaylist[].class));

            playlists.forEach(playlist -> {
                playlist.generateFeatures();
                batchPlaylists.putIfAbsent(playlist.getId(), playlist);
            });

            playlists.stream()
                .flatMap(playlist -> playlist.getTracks().values().stream())
                .forEach(track -> batchTracks.putIfAbsent(track.getId(), track));

            final boolean batchSizeReached = ((i + sliceSize) % (slicesPerBatch * sliceSize)) == 0;
            final boolean isLast = (i + sliceSize) == (offset + (slices * sliceSize));

            if (batchSizeReached || isLast) {
                int insertedPlaylists = database.insertDatasetPlaylists(batchPlaylists);
                log.info(">> INSERTED PLAYLISTS: " + insertedPlaylists);

                int insertedTracks = database.insertTracks(batchTracks);
                log.info(">> INSERTED TRACKS: " + insertedTracks);

                batchPlaylists.clear();
                batchTracks.clear();
            }
        }
    }
}
