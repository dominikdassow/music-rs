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
import java.util.List;
import java.util.stream.Collectors;

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

        final int slices = 1; // TODO
        final int sliceSize = 1000;

        for (int i = 0; i < (slices * sliceSize); i += sliceSize) {
            final String slice = i + "-" + (i + sliceSize - 1);

            log.info("> SLICE: " + slice);

            JsonNode root = objectMapper
                .readTree(new File("data/mpd/mpd.slice." + slice + ".json"));

            List<DatasetPlaylist> playlists
                = Arrays.asList(objectMapper.treeToValue(root.get("playlists"), DatasetPlaylist[].class));

            playlists.forEach(DatasetPlaylist::generateFeatures);

            int insertedPlaylists = database.insertDatasetPlaylists(playlists);

            log.info(">> INSERTED PLAYLISTS: " + insertedPlaylists);

            List<Track> tracks = playlists.stream()
                .flatMap(p -> p.getTracks().values().stream())
                .collect(Collectors.toList());

            int insertedTracks = database.insertTracks(tracks);

            log.info(">> INSERTED TRACKS: " + insertedTracks);
        }
    }
}
