package de.dominikdassow.musicrs.model;

import de.dominikdassow.musicrs.model.feature.PlaylistFeature;

import java.util.List;
import java.util.Map;

public interface AnyPlaylist
    extends AnyDocument {

    String getName();

    Map<Integer, Track> getTracks();

    List<PlaylistFeature> getFeatures();
}
