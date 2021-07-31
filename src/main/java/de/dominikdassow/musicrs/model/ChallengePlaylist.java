package de.dominikdassow.musicrs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.dominikdassow.musicrs.model.json.PlaylistTracksDeserializer;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

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
}
