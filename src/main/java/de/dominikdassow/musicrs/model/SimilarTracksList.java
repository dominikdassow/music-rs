package de.dominikdassow.musicrs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.Map;

@AllArgsConstructor
@RequiredArgsConstructor
public class SimilarTracksList {

    @Getter
    private final Map<Integer, Track> tracks;

    @With
    @Getter
    private double similarity;

    public static SimilarTracksList from(Playlist playlist) {
        return new SimilarTracksList(playlist.getTracks());
    }

    public boolean contains(Integer id) {
        return tracks.values().stream()
            .anyMatch(track -> track.getId().equals(id));
    }
}
