package de.dominikdassow.musicrs.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.dominikdassow.musicrs.model.feature.PlaylistFeature;
import de.dominikdassow.musicrs.model.json.PlaylistTracksDeserializer;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Document(collection = "challenge_set")
public class ChallengePlaylist
    implements AnyPlaylist {

    @Id
    @JsonProperty("pid")
    private Integer id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("num_tracks")
    private Integer numberOfTracks;

    @JsonProperty("num_holdouts")
    private Integer numberOfHoldouts;

    @JsonProperty("num_samples")
    private Integer numberOfSamples;

    @JsonProperty("modified_at")
    private Integer modifiedAt;

    @DBRef
    @JsonProperty("tracks")
    @JsonDeserialize(using = PlaylistTracksDeserializer.class)
    private Map<Integer, Track> tracks;

    @JsonIgnore
    private Map<String, Double> features = new HashMap<>();

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
}
