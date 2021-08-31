package de.dominikdassow.musicrs.recommender.objective;

import java.util.List;

public interface Objective {

    double evaluate(List<String> tracks);

    double evaluate(String track);
}
