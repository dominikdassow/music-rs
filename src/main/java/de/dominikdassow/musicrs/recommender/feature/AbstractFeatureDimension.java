package de.dominikdassow.musicrs.recommender.feature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractFeatureDimension<I, F>
    implements FeatureDimension<I> {

    protected final Map<I, Double> values = new HashMap<>();

    @Override
    public void add(I identifier) {
        values.put(identifier, values.getOrDefault(identifier, 0.0) + 1.0);
    }

    public List<F> getFeatures() {
        return values.entrySet().stream()
            .map(this::createFeature)
            .collect(Collectors.toList());
    }

    abstract protected F createFeature(Map.Entry<I, Double> entry);
}
