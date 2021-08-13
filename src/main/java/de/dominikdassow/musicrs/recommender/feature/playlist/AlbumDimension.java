package de.dominikdassow.musicrs.recommender.feature.playlist;

import de.dominikdassow.musicrs.model.Playlist;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.feature.PlaylistFeature;

public class AlbumDimension
    extends PlaylistFeatureDimension<String> {

    public AlbumDimension(Playlist playlist) {
        super(playlist);
    }

    @Override
    public double getWeight() {
        return 1.0;
    }

    @Override
    protected PlaylistFeature.Dimension getDimension() {
        return PlaylistFeature.Dimension.ALBUM;
    }

    @Override
    protected String parseTrack(Track track) {
        return track.getAlbumUri();
    }
}
