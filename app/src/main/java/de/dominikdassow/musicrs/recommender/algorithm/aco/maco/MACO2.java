package de.dominikdassow.musicrs.recommender.algorithm.aco.maco;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.Colony;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

import java.util.List;

public class MACO2<S extends Solution<T>, T>
    extends MACO<S, T> {

    public MACO2(Problem<S> problem, int numberOfAnts, int numberOfCycles, double alpha, double beta, double p) {
        super(problem, numberOfAnts, numberOfCycles, alpha, beta, p);
    }

    @Override
    protected List<Colony<S, T>> createColonies() {
        return MACO1.createColoniesWith(this, PheromoneFactorAggregation.SUMMED);
    }
}
