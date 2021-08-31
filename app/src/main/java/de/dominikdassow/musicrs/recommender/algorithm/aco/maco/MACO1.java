package de.dominikdassow.musicrs.recommender.algorithm.aco.maco;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.Colony;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.PheromoneTrail;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.colony.MultiObjectiveColonyWithMultiplePheromoneTrails;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.colony.SingleObjectiveColony;
import de.dominikdassow.musicrs.recommender.problem.GrowingProblem;
import de.dominikdassow.musicrs.recommender.solution.GrowingSolution;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
public class MACO1<S extends GrowingSolution<T>, T>
    extends MACO<S, T> {

    public MACO1(
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
        return createColoniesWith(this, PheromoneFactorAggregation.RANDOM);
    }

    protected static <S extends GrowingSolution<T>, T> List<Colony<S, T>> createColoniesWith(
        MACO<S, T> algorithm,
        MACO.PheromoneFactorAggregation aggregation
    ) {
        List<Colony<S, T>> colonies = new ArrayList<>();
        List<PheromoneTrail<T>> pheromoneTrails = new ArrayList<>();

        IntStream.range(0, algorithm.getProblem().getNumberOfObjectives()).forEach(i -> {
            PheromoneTrail<T> pheromoneTrail = new PheromoneTrail<>(algorithm.getProblem().getCandidates()) {{
                pheromoneTrails.add(this);
            }};

            colonies.add(new SingleObjectiveColony<>(algorithm, i, pheromoneTrail));
        });

        colonies.add(new MultiObjectiveColonyWithMultiplePheromoneTrails<>(algorithm, pheromoneTrails, aggregation));

        return colonies;
    }
}
