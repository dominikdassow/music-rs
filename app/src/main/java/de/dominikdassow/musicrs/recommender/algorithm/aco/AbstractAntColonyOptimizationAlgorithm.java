package de.dominikdassow.musicrs.recommender.algorithm.aco;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.Problem;

public abstract class AbstractAntColonyOptimizationAlgorithm<S, R>
    implements Algorithm<R> {

    protected final Problem<S> problem;

    public AbstractAntColonyOptimizationAlgorithm(Problem<S> problem) {
        this.problem = problem;
    }

    public Problem<S> getProblem() {
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
        initProgress();
        initParameters();

        while (!isStoppingConditionReached()) {
            createSolutions();
            performDaemonActions();
            updatePheromoneTrails();
            updateProgress();
        }
    }
}
