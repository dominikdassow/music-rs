package de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.MACO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.uma.jmetal.solution.Solution;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class Ant<S extends Solution<T>, T> {

    private final MACO<S, T> algorithm;
    private final Colony<S, T> colony;

    public Ant(MACO<S, T> algorithm, Colony<S, T> colony) {
        this.algorithm = algorithm;
        this.colony = colony;
    }

    public S createSolution() {
        S solution = algorithm.getProblem().createSolution();

        List<T> partialSolution = new ArrayList<>();
        List<T> candidates = new LinkedList<>(solution.getVariables());

        log.info("Ant::createSolution(), getNumberOfVariables=" + solution.getNumberOfVariables());

        for (int i = 0; i < solution.getNumberOfVariables(); i++) {
            log.info("Ant::createSolution(), i=" + i);

            EnumeratedDistribution<T> distribution
                = colony.getCandidateDistribution(partialSolution, candidates);

            T sample = distribution.sample();

            partialSolution.add(sample);
            candidates.remove(sample);
        }

        for (int i = 0; i < solution.getNumberOfVariables(); i++) {
            solution.setVariable(i, partialSolution.get(i));
        }

        return solution;
    }
}
