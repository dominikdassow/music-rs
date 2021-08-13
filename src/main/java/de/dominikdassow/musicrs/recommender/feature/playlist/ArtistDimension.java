package de.dominikdassow.musicrs.recommender.feature.playlist;

import de.dominikdassow.musicrs.model.Playlist;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.feature.PlaylistFeature;

public class ArtistDimension
    extends PlaylistFeatureDimension<String> {

    public ArtistDimension(Playlist playlist) {
        super(playlist);
    }

    @Override
    public double getWeight() {
        return 1.25;
    }

    @Override
    protected PlaylistFeature.Dimension getDimension() {
        return PlaylistFeature.Dimension.ARTIST;
    }

    @Override
    protected String parseTrack(Track track) {
        return track.getArtistUri();
    }
}
