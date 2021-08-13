package de.dominikdassow.musicrs.service.database;

import de.dominikdassow.musicrs.model.feature.PlaylistFeature;
import de.dominikdassow.musicrs.model.feature.TrackFeature;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates a unique numerical id for a {@code PlaylistFeature}, identified by its dimension and identifier.
 */
public class PlaylistFeatureIdGenerator
    extends IdGenerator {

    static {
        init(new AtomicInteger(-1), new ConcurrentHashMap<>());
    }

    public static Integer generate(PlaylistFeature.Dimension dimension, String identifier) {
        return generate(dimension + "#" + identifier);
    }
}
