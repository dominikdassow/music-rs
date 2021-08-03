package de.dominikdassow.musicrs.recommender.engine;

import de.dominikdassow.musicrs.model.DatasetPlaylist;
import de.dominikdassow.musicrs.model.feature.PlaylistFeature;
import de.dominikdassow.musicrs.recommender.data.PlaylistsFeaturesData;
import de.dominikdassow.musicrs.recommender.index.PlaylistFeatureIndex;
import de.dominikdassow.musicrs.recommender.neighborhood.user.DynamicTopKUserNeighborhood;
import de.dominikdassow.musicrs.repository.DatasetRepository;
import es.uam.eps.ir.ranksys.fast.preference.FastPreferenceData;
import es.uam.eps.ir.ranksys.nn.user.neighborhood.UserNeighborhood;
import es.uam.eps.ir.ranksys.nn.user.sim.SetCosineUserSimilarity;
import es.uam.eps.ir.ranksys.nn.user.sim.UserSimilarity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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

    public List<DatasetPlaylist> getResults(Integer id, int minNumberOfTracks) {
        UserNeighborhood<Integer> neighborhood = new DynamicTopKUserNeighborhood<>(similarity,
            neighbor -> datasetRepository.existsById(neighbor.v1),
            neighbors -> neighbors.stream().reduce(0,
                (sum, neighbor) -> sum + datasetRepository.countUniqueTracksById(neighbor.v1),
                Integer::sum) >= minNumberOfTracks);

        return neighborhood.getNeighbors(id)
            .sorted(Comparator.comparingDouble(result -> (-result.v2)))
            .map(result -> playlistFeatureIndex.uidx2user(result.v1))
            .map(playlistId -> datasetRepository.findById(playlistId).orElse(null)) // TODO
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
