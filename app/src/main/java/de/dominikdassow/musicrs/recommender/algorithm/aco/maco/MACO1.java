package de.dominikdassow.musicrs.recommender.algorithm.aco.maco;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.Colony;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.PheromoneTrail;
import lombok.extern.slf4j.Slf4j;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
public class MACO1<S extends Solution<T>, T>
    extends MACO<S, T> {

    public MACO1(Problem<S> problem, int numberOfAnts, int numberOfCycles, double alpha, double beta, double p) {
        super(problem, numberOfAnts, numberOfCycles, alpha, beta, p);
    }

    @Override
    protected List<Colony<S, T>> createColonies() {
        return createColoniesWith(this, PheromoneFactorAggregation.RANDOM);
    }

    protected static <S extends Solution<T>, T> List<Colony<S, T>> createColoniesWith(
        MACO<S, T> algorithm,
        MACO.PheromoneFactorAggregation aggregation
    ) {
        List<Colony<S, T>> colonies = new ArrayList<>();
        List<PheromoneTrail<S, T>> pheromoneTrails = new ArrayList<>();

        IntStream.range(0, algorithm.getProblem().getNumberOfObjectives()).forEach(i -> {
            PheromoneTrail<S, T> pheromoneTrail = new PheromoneTrail<>(algorithm.getCandidates()) {{
                pheromoneTrails.add(this);
            }};

            colonies.add(new Colony.SingleObjective<>(algorithm, i, pheromoneTrail));
        });

        colonies.add(new Colony.MultiObjective.MultiplePheromoneTrails<>(algorithm, pheromoneTrails, aggregation));

        return colonies;
    }
}
