package de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.MACO;
import de.dominikdassow.musicrs.recommender.solution.GrowingSolution;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public abstract class Colony<S extends GrowingSolution<T>, T> {

    protected final MACO<S, T> algorithm;

    protected final List<Ant<S, T>> ants = new ArrayList<>();
    protected final Map<Integer, S> globalBestSolutions = new ConcurrentHashMap<>();

    protected List<S> solutions;

    public Colony(MACO<S, T> algorithm) {
        this.algorithm = algorithm;

        IntStream.range(0, algorithm.getNumberOfAnts())
            .forEach(i -> ants.add(new Ant<>(algorithm, this)));
    }

    public void createSolutions() {
        solutions = ants.parallelStream()
            .map(ant -> {
                S solution = ant.createSolution();
                algorithm.getProblem().evaluate(solution);
                return solution;
            })
            .collect(Collectors.toList());
    }

    public void reset() {
        solutions.clear();
    }

    public Collection<S> getBestSolutions() {
        return globalBestSolutions.values();
    }

    public abstract void initPheromoneTrails();

    public abstract void findBestSolutions();

    public abstract void updatePheromoneTrails();

    protected abstract double getPheromoneFactor(S solution, T candidate);

    protected abstract double getHeuristicFactor(S solution, T candidate);

    protected EnumeratedDistribution<T> createCandidateDistribution(S solution, List<T> candidates) {
        Map<T, Double> pheromoneFactors = candidates.stream().collect(Collectors.toMap(
            Function.identity(),
            candidate -> algorithm.applyPheromoneFactorsWeight(getPheromoneFactor(solution, candidate)))
        );

        Map<T, Double> heuristicFactors = candidates.stream().collect(Collectors.toMap(
            Function.identity(),
            candidate -> algorithm.applyHeuristicFactorsWeight(getHeuristicFactor(solution, candidate)))
        );

        double summedCandidatesFactor = candidates.stream()
            .mapToDouble(candidate -> pheromoneFactors.get(candidate) * heuristicFactors.get(candidate))
            .sum();

        Map<T, Double> probabilities = candidates.stream().collect(Collectors.toMap(
            Function.identity(),
            candidate -> {
                double pheromoneFactor = pheromoneFactors.get(candidate);
                double heuristicFactor = heuristicFactors.get(candidate);
                double probability = (pheromoneFactor * heuristicFactor) / summedCandidatesFactor;

                if (probability == 0.0 || Double.isNaN(probability)) return Double.MIN_VALUE;

                return probability;
            }
        ));

        List<Pair<T, Double>> candidatesWithProbability = candidates.stream()
            .map(candidate -> new Pair<>(candidate, probabilities.get(candidate)))
            .collect(Collectors.toList());

        return new EnumeratedDistribution<>(candidatesWithProbability);
    }

    protected void updatePossibleGlobalBestSolution(int objective, S solution) {
        boolean update = !globalBestSolutions.containsKey(objective) ||
            algorithm.getProblem().isBetter(objective, solution, globalBestSolutions.get(objective));

        if (update) globalBestSolutions.put(objective, solution);
    }
}
