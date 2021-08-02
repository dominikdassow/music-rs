package de.dominikdassow.musicrs.recommender.feature;

public interface FeatureDimension<I> {

    double getWeight();

    void add(I identifier);
}
