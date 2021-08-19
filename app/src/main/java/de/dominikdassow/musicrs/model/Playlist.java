package de.dominikdassow.musicrs.model;

import de.dominikdassow.musicrs.model.feature.PlaylistFeature;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder(toBuilder = true)
public class Playlist {

    private Integer id;

    private Map<Integer, Track> tracks;

    private Set<PlaylistFeature> features;

    @Data
    @AllArgsConstructor
    public static class Sample {

        private Playlist playlist;

        private List<Track> originalTracks;

        public Integer getId() {
            return playlist.getId();
        }

        public Set<PlaylistFeature> getFeatures() {
            return playlist.getFeatures();
        }
    }
}
