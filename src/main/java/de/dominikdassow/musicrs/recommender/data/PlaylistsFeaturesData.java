package de.dominikdassow.musicrs.recommender.data;

import de.dominikdassow.musicrs.model.feature.PlaylistFeature;
import de.dominikdassow.musicrs.recommender.index.PlaylistFeatureIndex;
import es.uam.eps.ir.ranksys.fast.preference.IdxPref;
import org.ranksys.fast.preference.StreamsAbstractFastPreferenceData;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PlaylistsFeaturesData
    extends StreamsAbstractFastPreferenceData<Integer, PlaylistFeature> {

    private final PlaylistFeatureIndex index;

    public PlaylistsFeaturesData(PlaylistFeatureIndex playlistFeatureIndex) {
        super(playlistFeatureIndex, playlistFeatureIndex);

        this.index = playlistFeatureIndex;
    }

    @Override
    public int numUsers(int iidx) {
        return index.getPlaylistsByFeature().get(iidx).size();
    }

    @Override
    public int numItems(int uidx) {
        return index.getFeaturesByPlaylist().get(uidx).size();
    }

    @Override
    public IntStream getUidxWithPreferences() {
        return index.getAllUidx();
    }

    @Override
    public IntStream getIidxWithPreferences() {
        return index.getAllIidx();
    }

    @Override
    public Stream<IdxPref> getUidxPreferences(int uidx) {
        if (!index.getFeaturesByPlaylist().containsKey(uidx)) return Stream.empty();

        return index.getFeaturesByPlaylist().get(uidx).stream()
            .map(index::iidx2item)
            .map(playlistFeature -> new IdxPref(playlistFeature.getId(), playlistFeature.getValue()));
    }

    @Override
    public Stream<IdxPref> getIidxPreferences(int iidx) {
        if (!index.getPlaylistsByFeature().containsKey(iidx)) return Stream.empty();

        return index.getPlaylistsByFeature().get(iidx).stream()
            .map(playlistId -> new IdxPref(playlistId,
                index.getFeaturesByPlaylist().get(playlistId).stream()
                    .filter(playlistFeatureId -> playlistFeatureId.equals(iidx)).count()));
    }

    @Override
    public int numPreferences() {
        return index.numItems() * 2;
    }
}
