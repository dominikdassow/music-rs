package de.dominikdassow.musicrs.task;

import de.dominikdassow.musicrs.AppConfiguration;
import de.dominikdassow.musicrs.model.SimilarTracksList;
import de.dominikdassow.musicrs.recommender.engine.SimilarPlaylistsEngine;
import de.dominikdassow.musicrs.service.DatabaseService;
import de.dominikdassow.musicrs.util.Formatter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class GenerateSimilarTracksListsTask
    extends Task {

    private PrintWriter similarTracksListStore;

    private SimilarPlaylistsEngine similarPlaylistsEngine;

    public GenerateSimilarTracksListsTask() {
        super("Generate Similar Tracks Lists");
    }

    @Override
    protected void init() throws IOException {
        similarTracksListStore = DatabaseService.newStore(DatabaseService.Store.SIMILAR_TRACKS_LIST);
        similarPlaylistsEngine = new SimilarPlaylistsEngine();
    }

    @Override
    protected void execute() {
        DatabaseService.readAllPlaylistChallenges().parallel().forEach(playlist -> {
            log.info("*** " + playlist);

            if (DatabaseService.readPlaylistTracks(playlist).size() == 0) {
                similarPlaylistsEngine
                    .getRandomSimilarTracksFor(playlist, AppConfiguration.get().minNumberOfCandidateTracks)
                    .forEach(similarTracksList -> store(playlist, similarTracksList));
            } else {
                similarPlaylistsEngine
                    .getSimilarTracksFor(playlist, AppConfiguration.get().minNumberOfCandidateTracks)
                    .forEach(similarTracksList -> store(playlist, similarTracksList));
            }

            similarTracksListStore.flush();
        });
    }

    @Override
    protected void finish() {
        similarTracksListStore.close();
    }

    private void store(Integer playlist, SimilarTracksList similarTracksList) {
        similarTracksListStore.println(playlist
            + DatabaseService.DELIMITER
            + Formatter.format(similarTracksList.getSimilarity())
            + DatabaseService.DELIMITER
            + String.join(DatabaseService.DELIMITER, similarTracksList.getTracks()));
    }
}
