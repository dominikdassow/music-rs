package de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.MACO;
import de.dominikdassow.musicrs.recommender.solution.GrowingSolution;
import org.apache.commons.math3.distribution.EnumeratedDistribution;

import java.util.LinkedList;
import java.util.List;

public class Ant<S extends GrowingSolution<T>, T> {

    private final MACO<S, T> algorithm;
    private final Colony<S, T> colony;

    public Ant(MACO<S, T> algorithm, Colony<S, T> colony) {
        this.algorithm = algorithm;
        this.colony = colony;
    }

    public S createSolution() {
        S solution = algorithm.getProblem().createSolution();

        List<T> candidates = new LinkedList<>(algorithm.getProblem().getCandidates());

        for (int i = 0; i < solution.getNumberOfVariables(); i++) {
            EnumeratedDistribution<T> distribution = colony.createCandidateDistribution(candidates);

            T candidate = distribution.sample();

            solution.setVariable(i, candidate);
            candidates.remove(candidate);
        }

        return solution;
    }
}
