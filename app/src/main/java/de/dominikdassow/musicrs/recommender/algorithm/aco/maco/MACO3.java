package de.dominikdassow.musicrs.recommender.algorithm.aco.maco;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.Colony;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.PheromoneTrail;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class MACO3<S extends Solution<T>, T>
    extends MACO<S, T> {

    public MACO3(Problem<S> problem, int numberOfAnts, int numberOfCycles, double alpha, double beta, double p) {
        super(problem, numberOfAnts, numberOfCycles, alpha, beta, p);
    }

    @Override
    protected List<Colony<S, T>> createColonies() {
        List<Colony<S, T>> colonies = new ArrayList<>();

        colonies.add(new Colony.MultiObjective.SinglePheromoneTrail<>(this,
            new PheromoneTrail<>(getCandidates())));

        return colonies;
    }
}
