package de.dominikdassow.musicrs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.List;

@AllArgsConstructor
@RequiredArgsConstructor
public class SimilarTracksList {

    @Getter
    private final List<String> tracks;

    @With
    @Getter
    private double similarity;

    public boolean contains(String id) {
        return tracks.contains(id);
    }
}
