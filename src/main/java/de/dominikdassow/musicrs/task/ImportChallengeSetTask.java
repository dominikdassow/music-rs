package de.dominikdassow.musicrs.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dominikdassow.musicrs.model.playlist.ChallengePlaylist;
import de.dominikdassow.musicrs.service.DatabaseService;
import de.dominikdassow.musicrs.service.database.TrackIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@SuppressWarnings("unused")
public class ImportChallengeSetTask
    extends Task {

    @Autowired
    private DatabaseService database;

    private boolean rebuild = false;

    public ImportChallengeSetTask() {
        super("Import Challenge Set");
    }

    public ImportChallengeSetTask rebuilding(boolean rebuild) {
        this.rebuild = rebuild;

        return this;
    }

    @Override
    protected void init() {
        if (rebuild) database.resetChallengeSet();

        TrackIdGenerator.init(database.findAllTracksWithIdAndUri());
    }

    @Override
    protected void execute() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode root = objectMapper
            .readTree(new File("data/challenge/challenge_set.json"));

        List<ChallengePlaylist> playlists
            = Arrays.asList(objectMapper.treeToValue(root.get("playlists"), ChallengePlaylist[].class));

        playlists.forEach(ChallengePlaylist::generateFeatures);

        int insertedPlaylists = database.insertChallengePlaylists(playlists.stream()
            .collect(Collectors.toMap(ChallengePlaylist::getId, Function.identity())));

        log.info(">> INSERTED PLAYLISTS: " + insertedPlaylists);
    }
}
