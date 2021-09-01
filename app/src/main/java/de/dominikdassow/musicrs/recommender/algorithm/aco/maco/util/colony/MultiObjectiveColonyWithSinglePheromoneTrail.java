package de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.colony;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.MACO;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.Colony;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.PheromoneTrail;
import de.dominikdassow.musicrs.recommender.solution.GrowingSolution;
import org.uma.jmetal.util.SolutionListUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MultiObjectiveColonyWithSinglePheromoneTrail<S extends GrowingSolution<T>, T>
    extends Colony<S, T> {

    private final PheromoneTrail<T> pheromoneTrail;
    private final Map<Integer, List<S>> localBestSolutions = new HashMap<>();

    public MultiObjectiveColonyWithSinglePheromoneTrail(MACO<S, T> algorithm, PheromoneTrail<T> pheromoneTrail) {
        super(algorithm);

        this.pheromoneTrail = pheromoneTrail;
    }

    @Override
    public void findBestSolutions() {
        IntStream.range(0, algorithm.getProblem().getNumberOfObjectives()).forEach(objective -> {
            List<S> nonDominatedSolutions
                = SolutionListUtils.getNonDominatedSolutions(solutions);

            localBestSolutions.put(objective, nonDominatedSolutions);
            globalBestSolutions.get(objective).addAll(nonDominatedSolutions);
        });
    }

    @Override
    public void updatePheromoneTrails() {
        IntStream.range(0, algorithm.getProblem().getNumberOfObjectives()).forEach(objective -> {
            List<S> nonDominatedSolutions = localBestSolutions.get(objective);

            List<T> candidatesToReward = nonDominatedSolutions.stream()
                .flatMap(solution -> solution.getVariables().stream()
                    .filter(candidate -> algorithm.getProblem().isCandidateRewardedInSolution(candidate, solution)))
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
    protected double getPheromoneFactor(S solution, T candidate) {
        return pheromoneTrail.get(candidate);
    }

    @Override
    protected double getHeuristicFactor(S solution, T candidate) {
        return IntStream.range(0, algorithm.getProblem().getNumberOfObjectives())
            .mapToDouble(objective -> algorithm.getProblem().evaluate(candidate, objective))
            .sum();
    }

    @Override
    public void reset() {
        super.reset();

        localBestSolutions.clear();
    }
}
