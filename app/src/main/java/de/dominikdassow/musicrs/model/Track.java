package de.dominikdassow.musicrs.model;

import de.dominikdassow.musicrs.model.feature.TrackFeature;
import de.dominikdassow.musicrs.model.track.AudioFeatures;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
@Builder(toBuilder = true)
public class Track {

    private String id;

    private String artistId;

    private String albumId;

    private Set<TrackFeature> features;

    private AudioFeatures audioFeatures;

    public void setAudioFeaturesFrom(Map<String, Map<TrackFeature.Audio, Double>> audioFeatures) {
        if (!audioFeatures.containsKey(id)) return;

        setAudioFeatures(AudioFeatures.from(audioFeatures.get(id)));
    }
}
