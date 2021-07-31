package de.dominikdassow.musicrs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dominikdassow.musicrs.model.ChallengePlaylist;
import de.dominikdassow.musicrs.model.DatasetPlaylist;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.repository.ChallengeSetRepository;
import de.dominikdassow.musicrs.repository.DatasetRepository;
import de.dominikdassow.musicrs.repository.TrackRepository;
import de.dominikdassow.musicrs.service.database.TrackIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
@SuppressWarnings("unused")
public class DatabaseService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private ChallengeSetRepository challengeSetRepository;

    @Autowired
    private TrackRepository trackRepository;

    public void init() throws Exception {
        // TrackIdGenerator.init(mongoTemplate);

        // datasetRepository.deleteAll();
        // challengeSetRepository.deleteAll();
        // trackRepository.deleteAll();

        // importDataset();
        // importChallengeSet();
    }

    private void importDataset() throws Exception {
        StopWatch timer = new StopWatch();
        ObjectMapper objectMapper = new ObjectMapper();

        log.info("IMPORT DATASET");
        timer.start();

        final int slices = 100; // TODO
        final int sliceSize = 1000;

        for (int i = 0; i < (slices * sliceSize); i += sliceSize) {
            final String slice = i + "-" + (i + sliceSize - 1);

            log.info("> SLICE: " + slice);

            JsonNode root = objectMapper
                .readTree(new File("data/mpd/mpd.slice." + slice + ".json"));

            List<DatasetPlaylist> playlists = Stream
                .of(objectMapper.treeToValue(root.get("playlists"), DatasetPlaylist[].class))
                .collect(Collectors.toList());

            int insertedPlaylists = datasetRepository.insertMany(playlists, DatasetPlaylist.class);

            log.info(">> INSERTED PLAYLISTS: " + insertedPlaylists);

            int insertedTracks = trackRepository.insertMany(playlists.stream()
                .flatMap(p -> p.getTracks().values().stream())
                .collect(Collectors.toList()), Track.class);

            log.info(">> INSERTED TRACKS: " + insertedTracks);
        }

        timer.stop();
        log.info("+ TIME IN SECONDS: " + timer.getTotalTimeSeconds());
    }

    private void importChallengeSet() throws Exception {
        StopWatch timer = new StopWatch();
        ObjectMapper objectMapper = new ObjectMapper();

        log.info("IMPORT CHALLENGE SET");
        timer.start();

        JsonNode root = objectMapper
            .readTree(new File("data/challenge/challenge_set.json"));

        List<ChallengePlaylist> playlists
            = Arrays.asList(objectMapper.treeToValue(root.get("playlists"), ChallengePlaylist[].class));

        int insertedPlaylists = challengeSetRepository.insertMany(playlists, ChallengePlaylist.class);

        log.info(">> INSERTED PLAYLISTS: " + insertedPlaylists);

        timer.stop();
        log.info("+ TIME IN SECONDS: " + timer.getTotalTimeSeconds());
    }
}
