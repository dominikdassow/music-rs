package de.dominikdassow.musicrs.recommender.algorithm.aco.maco;

import lombok.extern.slf4j.Slf4j;
import org.uma.jmetal.algorithm.AlgorithmBuilder;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

@Slf4j
public class MACOBuilder<S extends Solution<T>, T>
    implements AlgorithmBuilder<MACO<S, T>> {

    private static final int DEFAULT_NUMBER_OF_ANTS = 100;
    private static final int DEFAULT_NUMBER_OF_CYCLES = 1000;
    private static final double DEFAULT_PHEROMONE_FACTORS_WEIGHT = 0.5;
    private static final double DEFAULT_HEURISTIC_FACTORS_WEIGHT = 0.5;
    private static final double DEFAULT_EVAPORATION_FACTOR = 0.1;

    private final Problem<S> problem;
    private final int variant;

    private int numberOfAnts;
    private int numberOfCycles;
    private double pheromoneFactorsWeight; // alpha
    private double heuristicFactorsWeight; // beta
    private double evaporationFactor; // p

    public MACOBuilder(Problem<S> problem, int variant) {
        this.problem = problem;
        this.variant = variant;

        numberOfAnts = DEFAULT_NUMBER_OF_ANTS;
        numberOfCycles = DEFAULT_NUMBER_OF_CYCLES;
        pheromoneFactorsWeight = DEFAULT_PHEROMONE_FACTORS_WEIGHT;
        heuristicFactorsWeight = DEFAULT_HEURISTIC_FACTORS_WEIGHT;
        evaporationFactor = DEFAULT_EVAPORATION_FACTOR;
    }

    public MACOBuilder<S, T> setNumberOfAnts(int numberOfAnts) {
        this.numberOfAnts = numberOfAnts;

        return this;
    }

    public MACOBuilder<S, T> setNumberOfCycles(int numberOfCycles) {
        this.numberOfCycles = numberOfCycles;

        return this;
    }

    public MACOBuilder<S, T> setPheromoneFactorsWeight(double pheromoneFactorsWeight) {
        this.pheromoneFactorsWeight = pheromoneFactorsWeight;

        return this;
    }

    public MACOBuilder<S, T> setHeuristicFactorsWeight(double heuristicFactorsWeight) {
        this.heuristicFactorsWeight = heuristicFactorsWeight;

        return this;
    }

    public MACOBuilder<S, T> setEvaporationFactor(double evaporationFactor) {
        this.evaporationFactor = evaporationFactor;

        return this;
    }

    @Override
    public MACO<S, T> build() {
        log.info("build(), variant=" + variant);

        switch (variant) {
            case 1:
                return new MACO1<>(problem, numberOfAnts, numberOfCycles,
                    pheromoneFactorsWeight, heuristicFactorsWeight, evaporationFactor);
            case 2:
                return new MACO2<>(problem, numberOfAnts, numberOfCycles,
                    pheromoneFactorsWeight, heuristicFactorsWeight, evaporationFactor);
            case 3:
                return new MACO3<>(problem, numberOfAnts, numberOfCycles,
                    pheromoneFactorsWeight, heuristicFactorsWeight, evaporationFactor);
            case 4:
                return new MACO4<>(problem, numberOfAnts, numberOfCycles,
                    pheromoneFactorsWeight, heuristicFactorsWeight, evaporationFactor);
            default:
                throw new IllegalStateException("Unexpected value: " + variant);
        }
    }
}
