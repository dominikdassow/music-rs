package de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class PheromoneTrail<T> {

    private static final double TAU_MIN = Double.MIN_VALUE;
    private static final double TAU_MAX = 1000.0; // TODO

    private final Map<T, Double> values;

    public PheromoneTrail(List<T> candidates) {
        values = new ConcurrentHashMap<>() {{
            candidates.forEach(candidate -> put(candidate, TAU_MAX));
        }};
    }

    public void init() {
        values.keySet().forEach(candidate -> values.put(candidate, TAU_MAX));
    }

    public double get(T candidate) {
        return values.get(candidate);
    }

    public void update(BiFunction<T, Double, Double> function) {
        values.replaceAll((candidate, value) -> {
            value = function.apply(candidate, value);

            if (value < TAU_MIN) value = TAU_MIN;
            else if (value > TAU_MAX) value = TAU_MAX;

            return value;
        });
    }
}
