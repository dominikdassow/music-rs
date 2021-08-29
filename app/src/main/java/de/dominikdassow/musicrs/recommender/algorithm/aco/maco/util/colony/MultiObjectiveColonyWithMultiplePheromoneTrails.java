package de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.colony;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.MACO;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.Colony;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.PheromoneTrail;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.List;
import java.util.stream.IntStream;

public class MultiObjectiveColonyWithMultiplePheromoneTrails<S extends Solution<T>, T>
    extends Colony<S, T> {

    private final List<PheromoneTrail<S, T>> pheromoneTrails;
    private final MACO.PheromoneFactorAggregation aggregation;

    public MultiObjectiveColonyWithMultiplePheromoneTrails(
        MACO<S, T> algorithm,
        List<PheromoneTrail<S, T>> pheromoneTrails,
        MACO.PheromoneFactorAggregation aggregation
    ) {
        super(algorithm);

        this.pheromoneTrails = pheromoneTrails;
        this.aggregation = aggregation;
    }

    @Override
    public void initPheromoneTrails() {
        pheromoneTrails.forEach(PheromoneTrail::init);
    }

    @Override
    public void updatePheromoneTrails() {
        IntStream.range(0, algorithm.getProblem().getNumberOfObjectives()).forEach(objective -> {
            S bestSolution
                = SolutionListUtils.findBestSolution(solutions, new ObjectiveComparator<>(objective));

            updatePossibleBestSolution(objective, bestSolution);

            pheromoneTrails.get(objective).update((candidate, value) -> {
                value = algorithm.applyEvaporationFactor(value);

                if (bestSolution.getVariables().contains(candidate)) {
                    value += 1 / (1 + bestSolution.getObjective(objective)
                        - bestSolutions.get(objective).getObjective(objective));
                }

                return value;
            });
        });
    }

    @Override
    protected double getPheromoneFactor(T candidate) {
        switch (aggregation) {
            case RANDOM:
                return pheromoneTrails
                    .get(JMetalRandom.getInstance().nextInt(0, pheromoneTrails.size() - 1))
                    .get(candidate);
            case SUMMED:
                return pheromoneTrails.stream()
                    .mapToDouble(pheromoneTrail -> pheromoneTrail.get(candidate))
                    .sum();
            default:
                throw new IllegalStateException("Unexpected value: " + aggregation);
        }
    }

    @Override
    protected double getHeuristicFactor(S solution) {
        return IntStream.range(0, solution.getNumberOfObjectives())
            .mapToDouble(solution::getObjective)
            .sum();
    }
}
