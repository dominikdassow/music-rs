package de.dominikdassow.musicrs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "dataset")
@CompoundIndexes({
    @CompoundIndex(name = "tracks_uri", def = "{ 'tracks.uri': 1 }")
})
public class DatasetPlaylist {
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

    @JsonProperty("tracks")
    private List<Track> tracks;
}
