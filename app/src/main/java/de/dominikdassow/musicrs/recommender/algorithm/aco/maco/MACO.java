package de.dominikdassow.musicrs.recommender.algorithm.aco.maco;

import com.google.common.collect.ImmutableList;
import de.dominikdassow.musicrs.recommender.algorithm.aco.AbstractAntColonyOptimizationAlgorithm;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.Colony;
import lombok.extern.slf4j.Slf4j;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class MACO<S extends Solution<T>, T>
    extends AbstractAntColonyOptimizationAlgorithm<S, List<S>> {

    public enum PheromoneFactorAggregation {RANDOM, SUMMED}

    private final int numberOfAnts;
    private final int numberOfCycles;
    private final double alpha; // Pheromone factors weight
    private final double beta; // Heuristic factors weight
    private final double p; // Evaporation factor

    private final List<T> candidates;

    private int currentCycle;

    protected List<Colony<S, T>> colonies;

    public MACO(
        Problem<S> problem,
        int numberOfAnts,
        int numberOfCycles,
        double alpha,
        double beta,
        double p
    ) {
        super(problem);

        this.numberOfAnts = numberOfAnts;
        this.numberOfCycles = numberOfCycles;
        this.alpha = alpha;
        this.beta = beta;
        this.p = p; // TODO: Assert 0 <= p <= 1

        candidates = ImmutableList.copyOf(problem.createSolution().getVariables());
    }

    abstract protected List<Colony<S, T>> createColonies();

    @Override
    protected void initProgress() {
        currentCycle = 0;
    }

    @Override
    protected void updateProgress() {
        currentCycle++;
    }

    @Override
    protected boolean isStoppingConditionReached() {
        return currentCycle >= numberOfCycles;
    }

    @Override
    protected void initParameters() {
        colonies = createColonies();
        colonies.forEach(Colony::initPheromoneTrails);
    }

    @Override
    protected void createSolutions() {
        colonies.parallelStream().forEach(Colony::createSolutions);
    }

    @Override
    protected void performDaemonActions() {}

    @Override
    protected void updatePheromoneTrails() {
        colonies.forEach(Colony::updatePheromoneTrails);
        colonies.forEach(Colony::reset);
    }

    public int getNumberOfAnts() {
        return numberOfAnts;
    }

    public double applyPheromoneFactorsWeight(double factor) {
        return Math.pow(factor, alpha);
    }

    public double applyHeuristicFactorsWeight(double factor) {
        return Math.pow(factor, beta);
    }

    public double applyEvaporationFactor(double value) {
        return value * (1 - p);
    }

    public List<T> getCandidates() {
        return candidates;
    }

    @Override
    public List<S> getResult() {
        return SolutionListUtils.getNonDominatedSolutions(new ArrayList<>() {{
            colonies.forEach(colony -> addAll(colony.getBestSolutions()));
        }});
    }

    @Override
    public String getName() {
        return "m-ACO";
    }

    @Override
    public String getDescription() {
        return "A generic ACO algorithm for multi-objective problems";
    }
}
