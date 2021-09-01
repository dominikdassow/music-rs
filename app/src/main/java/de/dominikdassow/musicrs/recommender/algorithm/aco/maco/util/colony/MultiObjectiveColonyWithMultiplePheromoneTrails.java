package de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.colony;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.MACO;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.Colony;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.PheromoneTrail;
import de.dominikdassow.musicrs.recommender.solution.GrowingSolution;
import lombok.extern.slf4j.Slf4j;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Slf4j
public class MultiObjectiveColonyWithMultiplePheromoneTrails<S extends GrowingSolution<T>, T>
    extends Colony<S, T> {

    private final List<PheromoneTrail<T>> pheromoneTrails;
    private final MACO.PheromoneFactorAggregation aggregation;
    private final Map<Integer, S> localBestSolutions = new ConcurrentHashMap<>();

    public MultiObjectiveColonyWithMultiplePheromoneTrails(
        MACO<S, T> algorithm,
        List<PheromoneTrail<T>> pheromoneTrails,
        MACO.PheromoneFactorAggregation aggregation
    ) {
        super(algorithm);

        this.pheromoneTrails = pheromoneTrails;
        this.aggregation = aggregation;
    }

    @Override
    public void findBestSolutions() {
        IntStream.range(0, algorithm.getProblem().getNumberOfObjectives()).forEach(objective -> {
            S localBestSolution
                = SolutionListUtils.findBestSolution(solutions, new ObjectiveComparator<>(objective));

            localBestSolutions.put(objective, localBestSolution);
            globalBestSolutions.get(objective).add(localBestSolution);
        });
    }

    @Override
    public void updatePheromoneTrails() {
        IntStream.range(0, algorithm.getProblem().getNumberOfObjectives()).forEach(objective -> {
            S localBestSolution = localBestSolutions.get(objective);

            S globalBestSolution = SolutionListUtils
                .findBestSolution(globalBestSolutions.get(objective), new ObjectiveComparator<>(objective));

            // TODO
            if (localBestSolution == null || globalBestSolution == null) {
                log.warn("objective=" + objective
                    + ", globalBestSolution=" + globalBestSolution
                    + ", localBestSolution=" + localBestSolution);
            }

            double localBestSolutionValue = algorithm.getProblem()
                .applyObjectiveValueNormalization(objective, localBestSolution.getObjective(objective));

            double globalBestSolutionValue = algorithm.getProblem()
                .applyObjectiveValueNormalization(objective, globalBestSolution.getObjective(objective));

            pheromoneTrails.get(objective).update((candidate, value) -> {
                value = algorithm.applyEvaporationFactor(value);

                if (algorithm.getProblem().isCandidateRewardedInSolution(candidate, localBestSolution)) {
                    value += 1 / (1 + localBestSolutionValue - globalBestSolutionValue);
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
    protected double getHeuristicFactor(T candidate) {
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
