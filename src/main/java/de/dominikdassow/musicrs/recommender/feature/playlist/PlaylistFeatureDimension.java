package de.dominikdassow.musicrs.recommender.feature.playlist;

import de.dominikdassow.musicrs.model.AnyPlaylist;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.feature.PlaylistFeature;
import de.dominikdassow.musicrs.recommender.feature.AbstractFeatureDimension;

import java.util.List;
import java.util.stream.Collectors;

public abstract class PlaylistFeatureDimension<I>
    extends AbstractFeatureDimension<I> {

    protected AnyPlaylist playlist;

    public PlaylistFeatureDimension(AnyPlaylist playlist) {
        this.playlist = playlist;

        playlist.getTracks().values().forEach(track -> add(parseTrack(track)));
    }

    public abstract PlaylistFeature.Dimension getDimension();

    protected abstract I parseTrack(Track track);

    public List<PlaylistFeature> getFeatures() {
        return values.entrySet().stream()
            .map(entry -> new PlaylistFeature(getDimension(), entry.getKey().toString(), entry.getValue()))
            .collect(Collectors.toList());
    }
}
