package de.dominikdassow.musicrs.recommender.engine;

import de.dominikdassow.musicrs.model.AnyPlaylist;
import de.dominikdassow.musicrs.model.PlaylistFeature;
import de.dominikdassow.musicrs.recommender.data.PlaylistsFeaturesData;
import de.dominikdassow.musicrs.recommender.index.PlaylistFeatureIndex;
import es.uam.eps.ir.ranksys.fast.preference.FastPreferenceData;
import es.uam.eps.ir.ranksys.nn.user.neighborhood.TopKUserNeighborhood;
import es.uam.eps.ir.ranksys.nn.user.neighborhood.UserNeighborhood;
import es.uam.eps.ir.ranksys.nn.user.sim.SetCosineUserSimilarity;
import es.uam.eps.ir.ranksys.nn.user.sim.UserSimilarity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@SuppressWarnings("unused")
public class SimilarPlaylistsEngine {

    @Autowired
    private PlaylistFeatureIndex playlistFeatureIndex;

    public List<AnyPlaylist> getResults(Integer id) {
        playlistFeatureIndex.init(); // TODO

        FastPreferenceData<AnyPlaylist, PlaylistFeature> preferenceData
            = new PlaylistsFeaturesData(playlistFeatureIndex);

        UserSimilarity<AnyPlaylist> similarity
            = new SetCosineUserSimilarity<>(preferenceData, 0.5, true);

        UserNeighborhood<AnyPlaylist> neighborhood
            = new TopKUserNeighborhood<>(similarity, 5);

        return neighborhood.getNeighbors(id)
            .peek(result -> {
                AnyPlaylist playlist = playlistFeatureIndex.uidx2user(result.v1);

                // TODO: Remove
                log.info("[" + playlist.getId() + "] " + playlist.getName() + " [similarity=" + result.v2 + "]");
            })
            .map(result -> playlistFeatureIndex.uidx2user(result.v1))
            .collect(Collectors.toList());
    }
}
