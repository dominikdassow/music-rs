package de.dominikdassow.musicrs.recommender.feature.playlist;

import de.dominikdassow.musicrs.model.Playlist;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.feature.PlaylistFeature;

public class TrackDimension
    extends PlaylistFeatureDimension<String> {

    public TrackDimension(Playlist playlist) {
        super(playlist);
    }

    @Override
    public double getWeight() {
        return 0.75;
    }

    @Override
    protected PlaylistFeature.Dimension getDimension() {
        return PlaylistFeature.Dimension.TRACK;
    }

    @Override
    protected String parseTrack(Track track) {
        return track.getUri();
    }
}
