package de.dominikdassow.musicrs.recommender.objective;

import de.dominikdassow.musicrs.recommender.engine.SimilarTracksEngine;

import java.util.List;

public class DiversityObjective
    implements Objective {

    private final SimilarTracksEngine similarTracksEngine;

    public DiversityObjective(SimilarTracksEngine similarTracksEngine) {
        this.similarTracksEngine = similarTracksEngine;
    }

    @Override
    public double evaluate(List<String> tracks) {
        double fitness = 0.0;

        for (int i = 0; i < tracks.size(); i++) {
            for (int j = i + 1; j < tracks.size(); j++) {
                fitness += similarTracksEngine.getDistanceBetween(tracks.get(i), tracks.get(j));
            }
        }

        return fitness;
    }

    @Override
    public double evaluate(String track) {
        return 0.0;
    }
}
