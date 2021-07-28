package de.dominikdassow.musicrs.recommender.objective;

import de.dominikdassow.musicrs.model.Track;

import java.util.List;

public interface Objective {
    double evaluate(List<Track> tracks);
}
