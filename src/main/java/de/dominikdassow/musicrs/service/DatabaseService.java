package de.dominikdassow.musicrs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dominikdassow.musicrs.model.DatasetPlaylist;
import de.dominikdassow.musicrs.repository.DatasetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@SuppressWarnings("unused")
public class DatabaseService {

    @Autowired
    private DatasetRepository datasetRepository;

    public void init() throws Exception {
        // datasetRepository.deleteAll();
        // importDataset();

        StopWatch timer = new StopWatch();
        timer.start();

        log.info("" + datasetRepository.countPlaylistsContainingTrackUri("spotify:track:0UaMYEvWZi0ZqiDOoHU3YI"));

        timer.stop();
        log.info("TIME IN SECONDS: " + timer.getTotalTimeSeconds());
    }

    private void importDataset() throws Exception {
        StopWatch timer = new StopWatch();
        ObjectMapper objectMapper = new ObjectMapper();

        timer.start();

        final int slices = 1000;
        final int sliceSize = 1000;

        for (int i = 0; i < (slices * sliceSize); i += sliceSize) {
            final String slice = i + "-" + (i + sliceSize - 1);

            log.info("SLICE: " + slice);

            JsonNode root = objectMapper
                .readTree(new File("data/mpd/mpd.slice." + slice + ".json"));

            List<DatasetPlaylist> playlists
                = Arrays.asList(objectMapper.treeToValue(root.get("playlists"), DatasetPlaylist[].class));

            log.info("> PLAYLISTS: " + playlists.size());
            datasetRepository.saveAll(playlists);
        }

        timer.stop();
        log.info("TIME IN SECONDS: " + timer.getTotalTimeSeconds());
    }
}
