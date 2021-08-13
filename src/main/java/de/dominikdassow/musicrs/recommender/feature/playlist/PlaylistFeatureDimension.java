package de.dominikdassow.musicrs.recommender.feature.playlist;

import de.dominikdassow.musicrs.model.Playlist;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.feature.PlaylistFeature;
import de.dominikdassow.musicrs.recommender.feature.AbstractFeatureDimension;

import java.util.Map;

public abstract class PlaylistFeatureDimension<I>
    extends AbstractFeatureDimension<I, PlaylistFeature> {

    protected Playlist playlist;

    public PlaylistFeatureDimension(Playlist playlist) {
        this.playlist = playlist;

        playlist.getTracks().values().forEach(track -> add(parseTrack(track)));
    }

    @Override
    protected PlaylistFeature createFeature(Map.Entry<I, Double> entry) {
        return new PlaylistFeature(getDimension(), entry.getKey().toString(), entry.getValue());
    }

    protected abstract PlaylistFeature.Dimension getDimension();

    protected abstract I parseTrack(Track track);
}
