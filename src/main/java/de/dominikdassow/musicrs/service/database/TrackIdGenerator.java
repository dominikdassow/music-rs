package de.dominikdassow.musicrs.service.database;

import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.feature.TrackFeature;
import lombok.Data;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates a unique numerical id for a {@code Track}, identified by its uri.
 */
public class TrackIdGenerator
    extends IdGenerator {

    public static void init(List<Track.WithIdAndUri> existing) {
        init(new AtomicInteger(-1), new HashMap<>());

        if (existing.isEmpty()) return;

        currentId.set(existing.get(existing.size() - 1).getId());

        existing.forEach(track ->
            TrackIdGenerator.existing.put(track.getUri(), track.getId()));
    }

    public static Integer generate(String uri) {
        return IdGenerator.generate(uri);
    }
}
