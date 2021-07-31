package de.dominikdassow.musicrs.model;

import java.util.Map;

public interface AnyPlaylist
    extends AnyDocument {

    String getName();

    Map<Integer, Track> getTracks();
}
