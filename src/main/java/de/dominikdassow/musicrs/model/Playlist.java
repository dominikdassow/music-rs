package de.dominikdassow.musicrs.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.dominikdassow.musicrs.model.feature.PlaylistFeature;
import de.dominikdassow.musicrs.model.json.PlaylistTracksDeserializer;
import de.dominikdassow.musicrs.recommender.feature.playlist.AlbumDimension;
import de.dominikdassow.musicrs.recommender.feature.playlist.ArtistDimension;
import de.dominikdassow.musicrs.recommender.feature.playlist.TrackDimension;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.AccessType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public abstract class Playlist
    implements Identifiable {

    @Id
    @Getter
    @JsonProperty("pid")
    private Integer id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("num_tracks")
    private Integer numberOfTracks;

    @JsonProperty("modified_at")
    private Integer modifiedAt;

    @DBRef
    @AccessType(AccessType.Type.PROPERTY)
    @JsonProperty("tracks")
    @JsonDeserialize(using = PlaylistTracksDeserializer.class)
    private Map<Integer, Track> tracks;

    @JsonIgnore
    private Map<String, Double> features;

    public void setTracks(Map<Integer, Track> tracks) {
        // Manually set the tracks since there seems to be a conversion issue with MongoDB.
        // The Integer-Track-Map is actually a String-Track-Map and leads to null values when getting the track list.
        this.tracks = new HashMap<>() {{
            for (Map.Entry<Integer, Track> entry : tracks.entrySet()) {
                put(Integer.valueOf(String.valueOf(entry.getKey())), entry.getValue());
            }
        }};
    }

    public void setFeatures(List<PlaylistFeature> features) {
        this.features = features.stream().collect(Collectors.toMap(
            feature -> feature.getDimension() + "#" + feature.getIdentifier(),
            PlaylistFeature::getValue
        ));
    }

    public List<PlaylistFeature> getFeatures() {
        return features.entrySet().stream().map(entry -> {
            String[] parts = entry.getKey().split("#");

            return new PlaylistFeature(PlaylistFeature.Dimension.valueOf(parts[0]), parts[1], entry.getValue());
        }).collect(Collectors.toList());
    }

    public void generateFeatures() {
        final List<PlaylistFeature> all = new ArrayList<>();

        new TrackDimension(this) {{
            all.addAll(getFeatures());
        }};

        new ArtistDimension(this) {{
            all.addAll(getFeatures());
        }};

        new AlbumDimension(this) {{
            all.addAll(getFeatures());
        }};

        setFeatures(all);
    }
}
