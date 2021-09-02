package de.dominikdassow.musicrs.task;

import de.dominikdassow.musicrs.AppConfiguration;
import de.dominikdassow.musicrs.model.SimilarTracksList;
import de.dominikdassow.musicrs.recommender.engine.SimilarPlaylistsEngine;
import de.dominikdassow.musicrs.service.DatabaseService;
import de.dominikdassow.musicrs.util.Formatter;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Slf4j
public class GenerateSimilarTracksListsTask
    extends Task {

    private PrintWriter similarTracksListStore;
    private SimilarPlaylistsEngine similarPlaylistsEngine;
    private Stream<Integer> playlists;

    public GenerateSimilarTracksListsTask() {
        super("Generate Similar Tracks Lists");
    }

    @Override
    protected void init() throws Exception {
        Map<Integer, List<SimilarTracksList>> existingSimilarTracksLists = DatabaseService
            .readSimilarTracksLists();

        if (existingSimilarTracksLists.isEmpty()) {
            log.info("Generating all similar tracks lists...");
            similarTracksListStore = DatabaseService.newStore(DatabaseService.Store.SIMILAR_TRACKS_LIST);
        } else {
            log.info("Found " + existingSimilarTracksLists.size() + " similar tracks lists");

            AtomicBoolean isCorrupted = new AtomicBoolean(false);

            existingSimilarTracksLists.forEach((playlist, similarTracksLists) -> {
                long numberOfUniqueTracks = similarTracksLists.stream()
                    .flatMap(similarTracksList -> similarTracksList.getTracks().stream())
                    .distinct()
                    .count();

                if (numberOfUniqueTracks < AppConfiguration.get().numberOfTracks) {
                    isCorrupted.set(true);
                    log.warn(playlist + " :: Not enough unique tracks (" + numberOfUniqueTracks + ")");
                }
            });

            if (isCorrupted.get()) {
                throw new Exception("Corrupted Similar Tracks Lists");
            }

            similarTracksListStore = DatabaseService.openStore(DatabaseService.Store.SIMILAR_TRACKS_LIST);
        }

        similarPlaylistsEngine = new SimilarPlaylistsEngine();

        playlists = DatabaseService
            .readAllPlaylistChallenges()
            .filter(playlist -> !existingSimilarTracksLists.containsKey(playlist));
    }

    @Override
    protected void execute() {
        playlists.parallel().forEach(playlist -> {
            log.info("*** " + playlist);

            if (similarPlaylistsEngine.getPlaylistTracks(playlist).size() == 0) {
                similarPlaylistsEngine
                    .getRandomSimilarTracksFor(playlist)
                    .forEach(similarTracksList -> store(playlist, similarTracksList));
            } else {
                similarPlaylistsEngine
                    .getSimilarTracksFor(playlist)
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
