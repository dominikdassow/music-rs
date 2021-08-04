package de.dominikdassow.musicrs.recommender.engine;

import de.dominikdassow.musicrs.model.DatasetPlaylist;
import de.dominikdassow.musicrs.model.feature.PlaylistFeature;
import de.dominikdassow.musicrs.model.playlist.SimilarPlaylist;
import de.dominikdassow.musicrs.recommender.data.PlaylistsFeaturesData;
import de.dominikdassow.musicrs.recommender.index.PlaylistFeatureIndex;
import de.dominikdassow.musicrs.recommender.neighborhood.user.DynamicTopKUserNeighborhood;
import de.dominikdassow.musicrs.repository.DatasetRepository;
import es.uam.eps.ir.ranksys.fast.preference.FastPreferenceData;
import es.uam.eps.ir.ranksys.nn.user.neighborhood.UserNeighborhood;
import es.uam.eps.ir.ranksys.nn.user.sim.SetCosineUserSimilarity;
import es.uam.eps.ir.ranksys.nn.user.sim.UserSimilarity;
import lombok.extern.slf4j.Slf4j;
import org.ranksys.core.util.tuples.Tuple2id;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@Slf4j
@SuppressWarnings("unused")
public class SimilarPlaylistsEngine {

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private PlaylistFeatureIndex playlistFeatureIndex;

    private FastPreferenceData<Integer, PlaylistFeature> data;

    private UserSimilarity<Integer> similarity;

    public void init() {
        playlistFeatureIndex.init(); // TODO

        data = new PlaylistsFeaturesData(playlistFeatureIndex);
        similarity = new SetCosineUserSimilarity<>(data, 0.5, true);
    }

    public List<SimilarPlaylist> getResults(Integer id, int minNumberOfTracks) {
        UserNeighborhood<Integer> neighborhood = new DynamicTopKUserNeighborhood<>(similarity,
            // TODO: Check statically?
            neighbor -> datasetRepository.existsById(playlistFeatureIndex.uidx2user(neighbor.v1)),
            neighbors -> {
                final List<Integer> ids
                    = neighbors.stream().map(Tuple2id::v1).collect(Collectors.toList());

                if (ids.isEmpty()) return false;

                return datasetRepository.countUniqueTracksByIds(ids) >= minNumberOfTracks;
            });

        AtomicInteger numberOfTracks = new AtomicInteger();

        return neighborhood.getNeighbors(id)
            .map(result -> {
                DatasetPlaylist playlist
                    = datasetRepository.findById(playlistFeatureIndex.uidx2user(result.v1)).orElse(null);

                if (playlist == null) return null;

                return new SimilarPlaylist(playlist, result.v2);
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingDouble(SimilarPlaylist::getSimilarity).reversed())
            .collect(Collectors.toList());
    }
}
