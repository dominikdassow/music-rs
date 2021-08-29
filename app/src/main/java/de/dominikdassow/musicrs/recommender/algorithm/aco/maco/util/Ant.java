package de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.MACO;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.uma.jmetal.solution.Solution;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Ant<S extends Solution<T>, T> {

    private final MACO<S, T> algorithm;
    private final Colony<S, T> colony;

    public Ant(MACO<S, T> algorithm, Colony<S, T> colony) {
        this.algorithm = algorithm;
        this.colony = colony;
    }

    public S createSolution() {
        S solution = algorithm.getProblem().createSolution();

        List<T> partialSolution = new ArrayList<>(solution.getNumberOfVariables());
        List<T> candidates = new LinkedList<>(solution.getVariables());

        for (int i = 0; i < solution.getNumberOfVariables(); i++) {
            EnumeratedDistribution<T> distribution
                = colony.createCandidateDistribution(partialSolution, candidates);

            T candidate = distribution.sample();

            partialSolution.add(candidate);
            candidates.remove(candidate);
        }

        for (int i = 0; i < solution.getNumberOfVariables(); i++) {
            solution.setVariable(i, partialSolution.get(i));
        }

        return solution;
    }
}
