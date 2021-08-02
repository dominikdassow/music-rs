package de.dominikdassow.musicrs.recommender.feature;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractFeatureDimension<I>
    implements FeatureDimension<I> {

    protected final Map<I, Double> values = new HashMap<>();

    @Override
    public void add(I identifier) {
        values.put(identifier, values.getOrDefault(identifier, 0.0) + 1.0);
    }
}
