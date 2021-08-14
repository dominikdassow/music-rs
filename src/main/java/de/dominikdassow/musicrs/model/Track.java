package de.dominikdassow.musicrs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.dominikdassow.musicrs.model.feature.TrackFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tracks")
public class Track
    implements Identifiable {

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

    // TODO: Use generateFeatures() + Store in DB
    public List<TrackFeature> getFeatures() {
        return List.of(
            new TrackFeature(TrackFeature.Dimension.ARTIST, artistUri),
            new TrackFeature(TrackFeature.Dimension.ALBUM, albumUri)
        );
    }

    @Data
    @Document(collection = "tracks")
    public static class WithIdAndUri {

        private Integer id;

        private String uri;
    }
}
