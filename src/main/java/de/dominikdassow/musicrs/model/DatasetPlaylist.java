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
@Document(collection = "dataset")
public class DatasetPlaylist
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

    @JsonProperty("num_artists")
    private Integer numberOfArtists;

    @JsonProperty("num_albums")
    private Integer numberOfAlbums;

    @JsonProperty("num_followers")
    private Integer numberOfFollowers;

    @JsonProperty("num_edits")
    private Integer numberOfEdits;

    @JsonProperty("duration_ms")
    private Integer duration;

    @JsonProperty("collaborative")
    private Boolean isCollaborative;

    @JsonProperty("modified_at")
    private Integer modifiedAt;

    @DBRef
    @JsonProperty("tracks")
    @JsonDeserialize(using = PlaylistTracksDeserializer.class)
    private Map<Integer, Track> tracks;
}
