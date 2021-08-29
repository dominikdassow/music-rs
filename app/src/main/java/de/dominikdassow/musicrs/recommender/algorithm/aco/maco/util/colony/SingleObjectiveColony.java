package de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.colony;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.MACO;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.Colony;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.PheromoneTrail;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.ObjectiveComparator;

import java.util.Comparator;

public class SingleObjectiveColony<S extends Solution<T>, T>
    extends Colony<S, T> {

    private final int objective;
    private final PheromoneTrail<S, T> pheromoneTrail;
    private final Comparator<S> solutionComparator;

    public SingleObjectiveColony(MACO<S, T> algorithm, int objective, PheromoneTrail<S, T> pheromoneTrail) {
        super(algorithm);

        this.objective = objective;
        this.pheromoneTrail = pheromoneTrail;

        solutionComparator = new ObjectiveComparator<>(objective);
    }

    @Override
    public void initPheromoneTrails() {
        pheromoneTrail.init();
    }

    @Override
    public void updatePheromoneTrails() {
        S bestSolution
            = SolutionListUtils.findBestSolution(solutions, solutionComparator);

        updatePossibleBestSolution(objective, bestSolution);

        pheromoneTrail.update((component, value) -> {
            value = algorithm.applyEvaporationFactor(value);

            if (bestSolution.getVariables().contains(component)) {
                value += 1 / (1 + bestSolution.getObjective(objective)
                    - bestSolutions.get(objective).getObjective(objective));
            }

            return value;
        });
    }

    @Override
    protected double getPheromoneFactor(T candidate) {
        return pheromoneTrail.get(candidate);
    }

    @Override
    protected double getHeuristicFactor(S solution) {
        return solution.getObjective(objective);
    }
}
