package de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util;

import org.uma.jmetal.solution.Solution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

// TODO: Remove S
public class PheromoneTrail<S extends Solution<T>, T> {

    private static final double TAU_MIN = Double.MIN_VALUE; // TODO
    private static final double TAU_MAX = 1_000_000.0; // TODO

    private final Map<T, Double> values;

    public PheromoneTrail(List<T> candidates) {
        values = new HashMap<>() {{
            candidates.forEach(candidate -> put(candidate, TAU_MAX));
        }};
    }

    public void init() {
        values.keySet().forEach(component -> values.put(component, TAU_MAX));
    }

    public double get(T component) {
        return values.get(component);
    }

    public void update(BiFunction<T, Double, Double> function) {
        values.replaceAll((component, value) -> {
            value = function.apply(component, value);

            if (value < TAU_MIN) value = TAU_MIN;
            else if (value > TAU_MAX) value = TAU_MAX;

            return value;
        });
    }
}
