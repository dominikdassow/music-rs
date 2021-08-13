package de.dominikdassow.musicrs.service.database;

import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class IdGenerator {

    protected static AtomicInteger currentId;

    protected static Map<String, Integer> existing;

    protected static void init(AtomicInteger currentId, Map<String, Integer> existing) {
        IdGenerator.currentId = currentId;
        IdGenerator.existing = existing;
    }

    protected static Integer generate(String key) {
        Assert.notNull(currentId, "IdGenerator::init() must be called before generate()");

        return existing.computeIfAbsent(key, t -> currentId.incrementAndGet());
    }
}
