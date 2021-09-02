package de.dominikdassow.musicrs.recommender.engine.playlist;

import de.dominikdassow.musicrs.AppConfiguration;
import es.uam.eps.ir.ranksys.nn.neighborhood.TopKNeighborhood;
import es.uam.eps.ir.ranksys.nn.user.neighborhood.UserNeighborhood;
import es.uam.eps.ir.ranksys.nn.user.sim.UserSimilarity;
import lombok.extern.slf4j.Slf4j;
import org.ranksys.core.util.tuples.Tuple2id;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class SimilarPlaylistNeighborhood
    extends UserNeighborhood<Integer> {

    public SimilarPlaylistNeighborhood(UserSimilarity<Integer> similarity, Predicate<List<Integer>> accept) {
        super(similarity, playlist -> {
            List<Tuple2id> neighbors = new ArrayList<>();

            int k = AppConfiguration.get().minNumberOfCandidateTracks /
                AppConfiguration.get().maxNumberOfTracksPerDatasetPlaylist;

            boolean accepted = false;

            while (!accepted) {
                neighbors.clear();

                new TopKNeighborhood(similarity, k).getNeighbors(playlist)
                    .filter(neighbor -> similarity.uidx2user(neighbor.v1)
                        < AppConfiguration.get().firstChallengeSetPlaylistId)
                    .forEach(neighbors::add);

                k += 1;
                accepted = !neighbors.isEmpty() && accept.test(neighbors.stream()
                    .map(neighbor -> similarity.uidx2user(neighbor.v1))
                    .collect(Collectors.toList()));
            }

            return neighbors.stream();
        });
    }
}
