package de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.MACO;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.uma.jmetal.solution.Solution;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class Colony<S extends Solution<T>, T> {

    protected final MACO<S, T> algorithm;

    protected final List<Ant<S, T>> ants = new ArrayList<>();
    protected final List<S> solutions = new ArrayList<>();
    protected final Map<Integer, S> bestSolutions = new HashMap<>();

    public Colony(MACO<S, T> algorithm) {
        this.algorithm = algorithm;

        IntStream.range(0, algorithm.getNumberOfAnts())
            .forEach(i -> ants.add(new Ant<>(algorithm, this)));
    }

    public void createSolutions() {
        ants.parallelStream().forEach(ant -> solutions.add(ant.createSolution()));
    }

    public void reset() {
        solutions.clear();
    }

    public Collection<S> getBestSolutions() {
        return bestSolutions.values();
    }

    public abstract void initPheromoneTrails();

    public abstract void updatePheromoneTrails();

    protected abstract double getPheromoneFactor(T candidate);

    protected abstract double getHeuristicFactor(S solution);

    protected EnumeratedDistribution<T> createCandidateDistribution(List<T> partialSolution, List<T> candidates) {
        Map<T, S> solutions = new ConcurrentHashMap<>() {{
            candidates.parallelStream().forEach(candidate -> {
                S solution = algorithm.getProblem().createSolution();

                for (int i = 0; i < solution.getNumberOfVariables(); i++) {
                    solution.setVariable(i, null);
                }

                for (int i = 0; i < partialSolution.size(); i++) {
                    solution.setVariable(i, partialSolution.get(i));
                }

                solution.setVariable(partialSolution.size(), candidate);

                algorithm.getProblem().evaluate(solution);

                put(candidate, solution);
            });
        }};

        Map<T, Double> pheromoneFactors = solutions.keySet().stream().collect(Collectors.toMap(
            Function.identity(),
            candidate -> algorithm.applyPheromoneFactorsWeight(getPheromoneFactor(candidate)))
        );

        Map<T, Double> heuristicFactors = solutions.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> algorithm.applyHeuristicFactorsWeight(getHeuristicFactor(entry.getValue()))
        ));

        double summedCandidatesFactor = candidates.stream()
            .mapToDouble(candidate -> pheromoneFactors.get(candidate) * heuristicFactors.get(candidate))
            .sum();

        Map<T, Double> probabilities = candidates.stream().collect(Collectors.toMap(
            Function.identity(),
            candidate -> {
                double pheromoneFactor = pheromoneFactors.get(candidate);
                double heuristicFactor = heuristicFactors.get(candidate);
                double probability = (pheromoneFactor * heuristicFactor) / summedCandidatesFactor;

                if (Double.isNaN(probability)) return Double.MIN_VALUE;

                return probability;
            }
        ));

        List<Pair<T, Double>> candidatesWithProbability = candidates.stream()
            .map(candidate -> new Pair<>(candidate, probabilities.get(candidate)))
            .collect(Collectors.toList());

        return new EnumeratedDistribution<>(candidatesWithProbability);
    }

    protected void updatePossibleBestSolution(int objective, S solution) {
        boolean update = !bestSolutions.containsKey(objective) ||
            solution.getObjective(objective) > bestSolutions.get(objective).getObjective(objective);

        if (update) bestSolutions.put(objective, solution);
    }
}
