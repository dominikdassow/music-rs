package de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.colony;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.MACO;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.Colony;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.PheromoneTrail;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MultiObjectiveColonyWithSinglePheromoneTrail<S extends Solution<T>, T>
    extends Colony<S, T> {

    private final PheromoneTrail<S, T> pheromoneTrail;

    public MultiObjectiveColonyWithSinglePheromoneTrail(MACO<S, T> algorithm, PheromoneTrail<S, T> pheromoneTrail) {
        super(algorithm);

        this.pheromoneTrail = pheromoneTrail;
    }

    @Override
    public void initPheromoneTrails() {
        pheromoneTrail.init();
    }

    @Override
    public void updatePheromoneTrails() {
        IntStream.range(0, algorithm.getProblem().getNumberOfObjectives()).forEach(objective -> {
            List<S> nonDominatedSolutions
                = SolutionListUtils.getNonDominatedSolutions(solutions);

            nonDominatedSolutions
                .forEach(solution -> updatePossibleBestSolution(objective, solution));

            List<T> candidatesToReward = nonDominatedSolutions
                .stream().flatMap(nonDominatedSolution -> nonDominatedSolution.getVariables().stream())
                .collect(Collectors.toList());

            pheromoneTrail.update((candidate, value) -> {
                value = algorithm.applyEvaporationFactor(value);

                if (candidatesToReward.contains(candidate)) {
                    value += 1;
                }

                return value;
            });
        });
    }

    @Override
    protected double getPheromoneFactor(T candidate) {
        return pheromoneTrail.get(candidate);
    }

    @Override
    protected double getHeuristicFactor(S solution) {
        return IntStream.range(0, solution.getNumberOfObjectives())
            .mapToDouble(solution::getObjective)
            .sum();
    }
}
