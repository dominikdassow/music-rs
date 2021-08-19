package de.dominikdassow.musicrs.recommender.objective;

import de.dominikdassow.musicrs.recommender.engine.SimilarTracksEngine;
import lombok.extern.slf4j.Slf4j;

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

        for (int i = 0; i < (tracks.size() - 1); i++) {
            fitness += 1.0 - similarTracksEngine.getDistanceBetween(tracks.get(i), tracks.get(i + 1));
        }

        return fitness;
    }
}
