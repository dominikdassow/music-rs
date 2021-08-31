package de.dominikdassow.musicrs.recommender.algorithm.aco.maco;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.Colony;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.PheromoneTrail;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.colony.MultiObjectiveColonyWithSinglePheromoneTrail;
import de.dominikdassow.musicrs.recommender.problem.GrowingProblem;
import de.dominikdassow.musicrs.recommender.solution.GrowingSolution;

import java.util.ArrayList;
import java.util.List;

public class MACO3<S extends GrowingSolution<T>, T>
    extends MACO<S, T> {

    public MACO3(
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
        List<Colony<S, T>> colonies = new ArrayList<>();

        colonies.add(new MultiObjectiveColonyWithSinglePheromoneTrail<>(this,
            new PheromoneTrail<>(problem.getCandidates())));

        return colonies;
    }
}
