package de.dominikdassow.musicrs.model.playlist;

import de.dominikdassow.musicrs.model.AnyPlaylist;
import de.dominikdassow.musicrs.model.DatasetPlaylist;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.feature.PlaylistFeature;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class SimilarPlaylist
    implements AnyPlaylist {

    private final DatasetPlaylist playlist;
    private final double similarity;

    @Override
    public Integer getId() {
        return playlist.getId();
    }

    @Override
    public String getName() {
        return playlist.getName();
    }

    @Override
    public Map<Integer, Track> getTracks() {
        return playlist.getTracks();
    }

    @Override
    public List<PlaylistFeature> getFeatures() {
        return playlist.getFeatures();
    }

    public boolean containsTrack(Integer id) {
        return playlist.getTracks().values().stream()
            .anyMatch(track -> track.getId().equals(id));
    }
}
