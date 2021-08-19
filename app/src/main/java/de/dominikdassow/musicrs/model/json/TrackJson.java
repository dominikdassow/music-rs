package de.dominikdassow.musicrs.model.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.dominikdassow.musicrs.model.Track;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackJson {

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

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class WithPosition
        extends TrackJson {

        @JsonProperty("pos")
        private Integer position;

        public Track asTrack() {
            return Track.builder()
                .id(getUri().split(":")[2])
                .artistId(getArtistUri().split(":")[2])
                .albumId(getAlbumUri().split(":")[2])
                .build();
        }
    }
}
