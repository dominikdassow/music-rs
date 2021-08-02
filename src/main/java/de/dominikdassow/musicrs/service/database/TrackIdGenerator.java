package de.dominikdassow.musicrs.service.database;

import de.dominikdassow.musicrs.model.Track;
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
public class TrackIdGenerator {

    private static AtomicInteger currentId;

    private static final Map<String, Integer> existing = new HashMap<>();

    public static void initWith(MongoTemplate mongoTemplate) {
        if (currentId != null) return;

        currentId = new AtomicInteger(-1);

        List<TrackWithIdAndUri> existing = mongoTemplate.find(new Query() {{
            fields().include("_id").include("uri");
            with(Sort.by(Sort.Direction.ASC, "_id"));
        }}, TrackWithIdAndUri.class, mongoTemplate.getCollectionName(Track.class));

        if (existing.isEmpty()) return;

        currentId.set(existing.get(existing.size() - 1).getId());
        existing.forEach(track ->
            TrackIdGenerator.existing.put(track.getUri(), track.getId()));
    }

    public static Integer generate(String uri) {
        Assert.notNull(currentId, "TrackIdGenerator::init() must be called before generate()");

        return existing.computeIfAbsent(uri, t -> currentId.incrementAndGet());
    }

    @Data
    private static class TrackWithIdAndUri {
        private Integer id;
        private String uri;
    }
}
