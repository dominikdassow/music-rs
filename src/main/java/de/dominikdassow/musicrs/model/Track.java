package de.dominikdassow.musicrs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tracks")
public class Track
    implements AnyDocument {

    @Id
    @With
    private Integer id;

    @Indexed(unique = true)
    @JsonProperty("track_uri")
    private String uri;

    @JsonProperty("track_name")
    private String name;

    @JsonProperty("artist_uri")
    private String artistUri;

    @JsonProperty("artist_name")
    private String artistName;

    @JsonProperty("album_uri")
    private String albumUri;

    @JsonProperty("album_name")
    private String albumName;

    @JsonProperty("duration_ms")
    private Integer duration;
}
