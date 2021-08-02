package de.dominikdassow.musicrs.model.feature;

import de.dominikdassow.musicrs.service.database.PlaylistFeatureIdGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlaylistFeature {

    public enum Dimension {
        TRACK,
        ARTIST,
        ALBUM,
    }

    private Dimension dimension;

    private String identifier;

    private Double value;

    public Integer getId() {
        return PlaylistFeatureIdGenerator.generate(dimension, identifier);
    }
}
