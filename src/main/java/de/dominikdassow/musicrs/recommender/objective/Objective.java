package de.dominikdassow.musicrs.recommender.objective;

import java.util.List;

public interface Objective {

    double evaluate(List<Integer> tracks);
}
