package de.dominikdassow.musicrs.model.playlist;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.dominikdassow.musicrs.model.Playlist;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "dataset")
public class DatasetPlaylist
    extends Playlist {

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
}
