package de.dominikdassow.musicrs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Track {
    @JsonProperty("pos")
    private Integer position;

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
