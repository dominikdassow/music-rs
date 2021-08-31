package de.dominikdassow.musicrs.recommender.algorithm.aco;

import de.dominikdassow.musicrs.recommender.problem.GrowingProblem;
import de.dominikdassow.musicrs.recommender.solution.GrowingSolution;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.util.JMetalLogger;

public abstract class AbstractAntColonyOptimizationAlgorithm<S extends GrowingSolution<T>, T, R>
    implements Algorithm<R> {

    protected final GrowingProblem<S, T> problem;

    public AbstractAntColonyOptimizationAlgorithm(GrowingProblem<S, T> problem) {
        this.problem = problem;
    }

    public GrowingProblem<S, T> getProblem() {
        return problem;
    }

    protected abstract void initProgress();

    protected abstract void updateProgress();

    protected abstract boolean isStoppingConditionReached();

    protected abstract void initParameters();

    protected abstract void createSolutions();

    protected abstract void performDaemonActions();

    protected abstract void updatePheromoneTrails();

    @Override
    public abstract R getResult();

    @Override
    public void run() {
        try {
            initProgress();
            initParameters();

            while (!isStoppingConditionReached()) {
                createSolutions();
                performDaemonActions();
                updatePheromoneTrails();
                updateProgress();
            }
        } catch (Exception e) {
            JMetalLogger.logger.severe(e.getMessage());
            e.printStackTrace();

            throw e;
        }
    }
}
