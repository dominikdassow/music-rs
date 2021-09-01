package de.dominikdassow.musicrs.recommender.engine;

import de.dominikdassow.musicrs.AppConfiguration;
import de.dominikdassow.musicrs.model.SimilarTracksList;
import de.dominikdassow.musicrs.recommender.engine.playlist.SimilarPlaylistNeighborhood;
import de.dominikdassow.musicrs.service.DatabaseService;
import es.uam.eps.ir.ranksys.fast.index.FastItemIndex;
import es.uam.eps.ir.ranksys.fast.index.FastUserIndex;
import es.uam.eps.ir.ranksys.fast.index.SimpleFastItemIndex;
import es.uam.eps.ir.ranksys.fast.index.SimpleFastUserIndex;
import es.uam.eps.ir.ranksys.fast.preference.FastPreferenceData;
import es.uam.eps.ir.ranksys.fast.preference.SimpleFastPreferenceData;
import es.uam.eps.ir.ranksys.nn.user.neighborhood.UserNeighborhood;
import es.uam.eps.ir.ranksys.nn.user.sim.SetCosineUserSimilarity;
import es.uam.eps.ir.ranksys.nn.user.sim.UserSimilarity;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;
import org.ranksys.core.util.tuples.Tuple2id;
import org.ranksys.core.util.tuples.Tuple2od;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SimilarPlaylistsEngine {

    private final UserSimilarity<Integer> similarity;

    public SimilarPlaylistsEngine() {
        log.info("SimilarPlaylistsEngine()");

        Stream<Integer> playlists = Stream.concat(
            DatabaseService
                .readStore(DatabaseService.Store.PLAYLIST)
                .map(data -> data.split(DatabaseService.DELIMITER))
                .filter(this::filterPlaylistIndex)
                .map(data -> Integer.parseInt(data[0])),
            additionalPlaylists()
        );

        Stream<String> playlistFeatures = Stream.concat(
            DatabaseService
                .readStore(DatabaseService.Store.PLAYLIST_FEATURE)
                .map(data -> data.split(DatabaseService.DELIMITER))
                .filter(this::filterPlaylistFeatureIndex)
                .map(data -> data[1]),
            additionalPlaylistFeatures()
        );

        Stream<Tuple3<Integer, String, Double>> playlistFeatureValues = Stream.concat(
            DatabaseService
                .readStore(DatabaseService.Store.PLAYLIST_FEATURE)
                .map(data -> data.split(DatabaseService.DELIMITER))
                .filter(this::filterPlaylistFeatureIndex)
                .map(data -> Tuple.tuple(Integer.parseInt(data[0]), data[1], Double.parseDouble(data[2]))),
            additionalPlaylistFeatureValues()
        );

        final FastUserIndex<Integer> playlistIndex
            = SimpleFastUserIndex.load(playlists.distinct());

        log.info("PlaylistIndex :: " + playlistIndex.numUsers());

        final FastItemIndex<String> playlistFeatureIndex
            = SimpleFastItemIndex.load(playlistFeatures.distinct());

        log.info("PlaylistFeatureIndex :: " + playlistFeatureIndex.numItems());

        final FastPreferenceData<Integer, String> data
            = SimpleFastPreferenceData.load(playlistFeatureValues, playlistIndex, playlistFeatureIndex);

        log.info("Data :: " + data.numPreferences());

        similarity = new SetCosineUserSimilarity<>(data, 0.5, true) {
            @Override
            public Stream<Tuple2id> similarElems(int idx) {
                return super.similarElems(idx)
                    .filter(element -> data.uidx2user(element.v1) < AppConfiguration.get().firstChallengeSetPlaylistId);
            }
        };
    }

    public List<SimilarTracksList> getSimilarTracksFor(Integer playlist, int minNumberOfTracks) {
        UserNeighborhood<Integer> neighborhood
            = new SimilarPlaylistNeighborhood(similarity, minNumberOfTracks);

        return getSimilarTracksListsFromNeighbors(neighborhood.getNeighbors(playlist));
    }

    public List<SimilarTracksList> getRandomSimilarTracksFor(Integer playlist, int minNumberOfTracks) {
        List<Tuple2od<Integer>> neighbors = new ArrayList<>();

        int max = (int) DatabaseService
            .readStore(DatabaseService.Store.PLAYLIST)
            .filter(data -> Integer.parseInt(data.split(DatabaseService.DELIMITER)[0])
                < AppConfiguration.get().firstChallengeSetPlaylistId)
            .count();

        int k = minNumberOfTracks / AppConfiguration.get().minNumberOfTracksPerDatasetPlaylist;
        boolean accepted = false;

        while (!accepted) {
            neighbors.clear();

            new Random(playlist).ints(0, max)
                .distinct()
                .limit(k)
                .forEach(i -> neighbors.add(new Tuple2od<>(i, 0.1 + (0.5 - 0.1) * new Random(i).nextDouble())));

            k += 1;
            accepted = !neighbors.isEmpty() && DatabaseService
                .readNumberOfUniquePlaylistTracks(neighbors.stream()
                    .map(Tuple2od::v1)
                    .collect(Collectors.toList())) >= minNumberOfTracks;
        }

        return getSimilarTracksListsFromNeighbors(neighbors.stream());
    }

    private static List<SimilarTracksList> getSimilarTracksListsFromNeighbors(Stream<Tuple2od<Integer>> neighbors) {
        return neighbors
            .map(result -> new SimilarTracksList(new ArrayList<>(
                DatabaseService.readPlaylistTracks(result.v1).values()
            )).withSimilarity(result.v2))
            .sorted(Comparator.comparingDouble(SimilarTracksList::getSimilarity).reversed())
            .collect(Collectors.toList());
    }

    public boolean filterPlaylistIndex(String[] data) {
        return true;
    }

    public boolean filterPlaylistFeatureIndex(String[] data) {
        return true;
    }

    public Stream<Integer> additionalPlaylists() {
        return Stream.empty();
    }

    public Stream<String> additionalPlaylistFeatures() {
        return Stream.empty();
    }

    public Stream<Tuple3<Integer, String, Double>> additionalPlaylistFeatureValues() {
        return Stream.empty();
    }
}
