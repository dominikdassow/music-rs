package de.dominikdassow.musicrs.recommender.algorithm.aco.maco;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.Colony;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.PheromoneTrail;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.colony.MultiObjectiveColonyWithMultiplePheromoneTrails;
import de.dominikdassow.musicrs.recommender.problem.GrowingProblem;
import de.dominikdassow.musicrs.recommender.solution.GrowingSolution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class MACO4<S extends GrowingSolution<T>, T>
    extends MACO<S, T> {

    public MACO4(
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
        List<PheromoneTrail<T>> pheromoneTrails = new ArrayList<>();

        IntStream.range(0, problem.getNumberOfObjectives())
            .forEach(i -> pheromoneTrails.add(new PheromoneTrail<>(problem.getCandidates())));

        colonies.add(new MultiObjectiveColonyWithMultiplePheromoneTrails<>(this,
            pheromoneTrails, PheromoneFactorAggregation.RANDOM));

        return colonies;
    }
}
