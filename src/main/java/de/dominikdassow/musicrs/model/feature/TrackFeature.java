package de.dominikdassow.musicrs.model.feature;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrackFeature {

    public static final String DELIMITER = "#";

    public enum Dimension {
        ARTIST,
        ALBUM,
        AUDIO,
    }

    public enum Audio {
        ACOUSTICNESS,
        DANCEABILITY,
        ENERGY,
        INSTRUMENTALNESS,
        KEY,
        LIVENESS,
        LOUDNESS,
        MODE,
        SPEECHINESS,
        TEMPO,
        TIME_SIGNATURE,
        VALENCE
    }

    private Dimension dimension;

    private String identifier;

    private Double value;

    public static TrackFeature fromArtist(String artist, Double value) {
        return new TrackFeature(TrackFeature.Dimension.ARTIST, artist, value);
    }

    public static TrackFeature fromAlbum(String album, Double value) {
        return new TrackFeature(Dimension.ALBUM, album, value);
    }

    public static TrackFeature fromAudio(Audio audio, Double value) {
        return new TrackFeature(Dimension.AUDIO, audio.name(), value);
    }

    public String getId() {
        return dimension.name() + DELIMITER + identifier;
    }
}
