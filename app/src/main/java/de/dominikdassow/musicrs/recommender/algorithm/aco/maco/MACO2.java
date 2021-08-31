package de.dominikdassow.musicrs.recommender.algorithm.aco.maco;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.Colony;
import de.dominikdassow.musicrs.recommender.problem.GrowingProblem;
import de.dominikdassow.musicrs.recommender.solution.GrowingSolution;

import java.util.List;

public class MACO2<S extends GrowingSolution<T>, T>
    extends MACO<S, T> {

    public MACO2(
        GrowingProblem<S, T> problem,
        int numberOfAnts,
        int numberOfCycles,
        double alpha,
        double beta,
        double p
    ) {
        super(problem, numberOfAnts, numberOfCycles, alpha, beta, p);
    }

    @Override
    protected List<Colony<S, T>> createColonies() {
        return MACO1.createColoniesWith(this, PheromoneFactorAggregation.SUMMED);
    }
}
