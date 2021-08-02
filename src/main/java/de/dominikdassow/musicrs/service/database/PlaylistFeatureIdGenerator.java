package de.dominikdassow.musicrs.service.database;

import de.dominikdassow.musicrs.model.feature.PlaylistFeature;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates a unique numerical id for a {@code PlaylistFeature}, identified by its dimension and identifier.
 */
public class PlaylistFeatureIdGenerator {

    private static final AtomicInteger currentId = new AtomicInteger(-1);

    private static final ConcurrentMap<String, Integer> existing = new ConcurrentHashMap<>();

    public static Integer generate(PlaylistFeature.Dimension dimension, String identifier) {
        return existing.computeIfAbsent(dimension + "#" + identifier, t -> currentId.incrementAndGet());
    }
}
