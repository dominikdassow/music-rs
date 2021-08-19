package de.dominikdassow.musicrs.model.track;

import de.dominikdassow.musicrs.model.feature.TrackFeature;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class AudioFeatures {
    private final Double acousticness;

    private final Double danceability;

    private final Double energy;

    private final Double instrumentalness;

    // private final Integer key;

    private final Double liveness;

    private final Double loudness;

    // private final Integer mode;

    private final Double speechiness;

    private final Double tempo;

    // private final Integer timeSignature;

    private final Double valence;

    public static AudioFeatures from(Map<TrackFeature.Audio, Double> audioFeatures) {
        return new AudioFeatures(
            audioFeatures.get(TrackFeature.Audio.ACOUSTICNESS),
            audioFeatures.get(TrackFeature.Audio.DANCEABILITY),
            audioFeatures.get(TrackFeature.Audio.ENERGY),
            audioFeatures.get(TrackFeature.Audio.INSTRUMENTALNESS),
            // audioFeatures.get(TrackFeature.Audio.KEY),
            audioFeatures.get(TrackFeature.Audio.LIVENESS),
            audioFeatures.get(TrackFeature.Audio.LOUDNESS),
            // audioFeatures.get(TrackFeature.Audio.MODE),
            audioFeatures.get(TrackFeature.Audio.SPEECHINESS),
            audioFeatures.get(TrackFeature.Audio.TEMPO),
            // audioFeatures.get(TrackFeature.Audio.TIME_SIGNATURE),
            audioFeatures.get(TrackFeature.Audio.VALENCE)
        );
    }
}
