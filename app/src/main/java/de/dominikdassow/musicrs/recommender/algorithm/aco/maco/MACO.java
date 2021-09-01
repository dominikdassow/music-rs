package de.dominikdassow.musicrs.recommender.algorithm.aco.maco;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util.Colony;
import de.dominikdassow.musicrs.recommender.problem.GrowingProblem;
import de.dominikdassow.musicrs.recommender.solution.GrowingSolution;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.SolutionListUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class MACO<S extends GrowingSolution<T>, T>
    implements Algorithm<List<S>> {

    public enum PheromoneFactorAggregation {RANDOM, SUMMED}

    protected final GrowingProblem<S, T> problem;

    private final int numberOfAnts;
    private final int numberOfCycles;
    private final double alpha; // Pheromone factors weight
    private final double beta; // Heuristic factors weight
    private final double p; // Evaporation factor

    protected List<Colony<S, T>> colonies;

    public MACO(
        GrowingProblem<S, T> problem,
        int numberOfAnts,
        int numberOfCycles,
        double alpha,
        double beta,
        double p
    ) {
        this.problem = problem;
        this.numberOfAnts = numberOfAnts;
        this.numberOfCycles = numberOfCycles;
        this.alpha = alpha;
        this.beta = beta;
        this.p = p;
    }

    abstract protected List<Colony<S, T>> createColonies();

    public GrowingProblem<S, T> getProblem() {
        return problem;
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

    @Override
    public List<S> getResult() {
        return SolutionListUtils.getNonDominatedSolutions(new ArrayList<>() {{
            colonies.forEach(colony -> addAll(colony.getBestSolutions()));
        }});
    }

    @Override
    public void run() {
        try {
            int currentCycle = 0;

            colonies = createColonies();
            colonies.forEach(Colony::initHeuristicFactors);

            while (currentCycle < numberOfCycles) {
                colonies.forEach(colony -> {
                    colony.initPheromoneFactors();
                    colony.createSolutions();
                    colony.findBestSolutions();
                    colony.updatePheromoneTrails();
                    colony.reset();
                });

                currentCycle++;
            }
        } catch (Exception e) {
            JMetalLogger.logger.severe(e.getMessage());
            e.printStackTrace();

            throw e;
        }
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
