package de.dominikdassow.musicrs.service.database;

import de.dominikdassow.musicrs.model.PlaylistFeature;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates a unique numerical id for a {@code PlaylistFeature}, identified by its playlist id, type, and identifier.
 */
public class PlaylistFeatureIdGenerator {

    private static final AtomicInteger currentId = new AtomicInteger(-1);

    private static final Map<String, Integer> existing = new HashMap<>();

    public static Integer generate(PlaylistFeature.Type type, String identifier) {
        return existing.computeIfAbsent(type + ":" + identifier, t -> currentId.incrementAndGet());
    }
}
