package de.dominikdassow.musicrs.recommender.engine;

import de.dominikdassow.musicrs.model.AnyPlaylist;
import de.dominikdassow.musicrs.model.feature.PlaylistFeature;
import de.dominikdassow.musicrs.recommender.data.PlaylistsFeaturesData;
import de.dominikdassow.musicrs.recommender.index.PlaylistFeatureIndex;
import de.dominikdassow.musicrs.repository.DatasetRepository;
import es.uam.eps.ir.ranksys.fast.preference.FastPreferenceData;
import es.uam.eps.ir.ranksys.nn.item.ItemNeighborhoodRecommender;
import es.uam.eps.ir.ranksys.nn.user.UserNeighborhoodRecommender;
import es.uam.eps.ir.ranksys.nn.user.neighborhood.TopKUserNeighborhood;
import es.uam.eps.ir.ranksys.nn.user.neighborhood.UserNeighborhood;
import es.uam.eps.ir.ranksys.nn.user.sim.SetCosineUserSimilarity;
import es.uam.eps.ir.ranksys.nn.user.sim.UserSimilarity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@SuppressWarnings("unused")
public class SimilarPlaylistsEngine {

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private PlaylistFeatureIndex playlistFeatureIndex;

    public List<AnyPlaylist> getResults(Integer id, int k) {
        playlistFeatureIndex.init(); // TODO

        FastPreferenceData<Integer, PlaylistFeature> preferenceData
            = new PlaylistsFeaturesData(playlistFeatureIndex);

        UserSimilarity<Integer> similarity
            = new SetCosineUserSimilarity<>(preferenceData, 0.5, true);

        UserNeighborhood<Integer> neighborhood
            = new TopKUserNeighborhood<>(similarity, k);

        return neighborhood.getNeighbors(id)
            .sorted(Comparator.comparingDouble(result -> (-result.v2)))
            .map(result -> playlistFeatureIndex.uidx2user(result.v1))
            .map(playlistId -> datasetRepository.findById(playlistId).orElseThrow()) // TODO
            .collect(Collectors.toList());
    }
}
