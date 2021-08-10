package de.dominikdassow.musicrs.model.feature;

import de.dominikdassow.musicrs.service.database.TrackFeatureIdGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrackFeature {

    public enum Dimension {
        ARTIST,
        ALBUM,
    }

    private Dimension dimension;

    private String identifier;

    public Integer getId() {
        return TrackFeatureIdGenerator.generate(dimension, identifier);
    }
}
