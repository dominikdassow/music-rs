package de.dominikdassow.musicrs.recommender.objective;

import de.dominikdassow.musicrs.model.Track;

import java.util.List;

public class DiversityObjective
    implements Objective {

    public DiversityObjective() {
    }

    @Override
    public double evaluate(List<Track> tracks) {
        return 0; // TODO
    }
}
