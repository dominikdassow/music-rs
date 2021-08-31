package de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.colony;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.MACO;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.Colony;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.PheromoneTrail;
import de.dominikdassow.musicrs.recommender.solution.GrowingSolution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.ObjectiveComparator;

import java.util.Comparator;

public class SingleObjectiveColony<S extends GrowingSolution<T>, T>
    extends Colony<S, T> {

    private final int objective;
    private final PheromoneTrail<T> pheromoneTrail;
    private final Comparator<S> solutionComparator;

    private S localBestSolution;

    public SingleObjectiveColony(MACO<S, T> algorithm, int objective, PheromoneTrail<T> pheromoneTrail) {
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
    public void findBestSolutions() {
        localBestSolution
            = SolutionListUtils.findBestSolution(solutions, solutionComparator);

        updatePossibleGlobalBestSolution(objective, localBestSolution);
    }

    @Override
    public void updatePheromoneTrails() {
        double localBestSolutionValue = algorithm.getProblem()
            .applyObjectiveValueNormalization(objective, localBestSolution.getObjective(objective));

        double globalBestSolutionValue = algorithm.getProblem()
            .applyObjectiveValueNormalization(objective, globalBestSolutions.get(objective).getObjective(objective));

        pheromoneTrail.update((candidate, value) -> {
            value = algorithm.applyEvaporationFactor(value);

            if (algorithm.getProblem().isCandidateRewardedInSolution(candidate, localBestSolution)) {
                value += 1 / (1 + localBestSolutionValue - globalBestSolutionValue);
            }

            return value;
        });
    }

    @Override
    protected double getPheromoneFactor(S solution, T candidate) {
        return pheromoneTrail.get(candidate);
    }

    @Override
    protected double getHeuristicFactor(S solution, T candidate) {
        return algorithm.getProblem().evaluateCandidate(candidate, objective);
    }

    @Override
    public void reset() {
        super.reset();

        localBestSolution = null;
    }
}
