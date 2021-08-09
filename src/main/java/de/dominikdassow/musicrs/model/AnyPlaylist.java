package de.dominikdassow.musicrs.model;

import de.dominikdassow.musicrs.model.feature.PlaylistFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface AnyPlaylist
    extends AnyDocument {

    String getName();

    Map<Integer, Track> getTracks();

    List<PlaylistFeature> getFeatures();

    default List<Track> getTrackList() {
        return new ArrayList<>() {{
            for (Map.Entry<Integer, Track> entry : getTracks().entrySet()) {
                add(Integer.parseInt(String.valueOf(entry.getKey())), entry.getValue());
            }
        }};
    }
}
