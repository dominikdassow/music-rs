package de.dominikdassow.musicrs.model;

import de.dominikdassow.musicrs.service.database.PlaylistFeatureIdGenerator;
import lombok.Data;

@Data
public class PlaylistFeature {

    public enum Type {
        TRACK,
        ARTIST,
        ALBUM,
    }

    private Integer id;

    private Type type;

    private String identifier;

    private double value;

    public PlaylistFeature(Type type, String identifier, double value) {
        this.id = PlaylistFeatureIdGenerator.generate(type, identifier);
        this.type = type;
        this.identifier = identifier;
        this.value = value;
    }
}
