package de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.MACO;
import de.dominikdassow.musicrs.recommender.solution.GrowingSolution;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.uma.jmetal.util.SolutionListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public abstract class Colony<S extends GrowingSolution<T>, T> {

    protected final MACO<S, T> algorithm;
    protected final List<Ant<S, T>> ants;
    protected final Map<Integer, List<S>> globalBestSolutions;

    protected Map<T, Double> pheromoneFactors;
    protected Map<T, Double> heuristicFactors;

    protected List<S> solutions;

    public Colony(MACO<S, T> algorithm) {
        this.algorithm = algorithm;

        ants = IntStream.range(0, algorithm.getNumberOfAnts())
            .boxed()
            .map(i -> new Ant<>(algorithm, this))
            .collect(Collectors.toList());

        globalBestSolutions = new ConcurrentHashMap<>(algorithm.getProblem().getNumberOfObjectives()) {{
            IntStream.range(0, algorithm.getProblem().getNumberOfObjectives())
                .forEach(objective -> put(objective, new ArrayList<>()));
        }};
    }

    public void initPheromoneFactors() {
        pheromoneFactors = algorithm.getProblem().getCandidates().stream().collect(Collectors.toMap(
            Function.identity(),
            candidate -> algorithm.applyPheromoneFactorsWeight(getPheromoneFactor(candidate)))
        );
    }

    public void initHeuristicFactors() {
        heuristicFactors = algorithm.getProblem().getCandidates().stream().collect(Collectors.toMap(
            Function.identity(),
            candidate -> algorithm.applyHeuristicFactorsWeight(getHeuristicFactor(candidate)))
        );
    }

    public void createSolutions() {
        solutions = ants.parallelStream()
            .map(Ant::createSolution)
            .peek(solution -> algorithm.getProblem().evaluate(solution))
            .collect(Collectors.toList());

        // TODO
        if (solutions.size() != ants.size()) {
            log.info("#solution=" + solutions.size() + " :: #ants=" + ants.size());
        }
    }

    public void reset() {
        solutions.clear();
    }

    public List<S> getBestSolutions() {
        return SolutionListUtils.getNonDominatedSolutions(new ArrayList<>() {{
            globalBestSolutions.values().forEach(this::addAll);
        }});
    }

    public abstract void findBestSolutions();

    public abstract void updatePheromoneTrails();

    protected abstract double getPheromoneFactor(T candidate);

    protected abstract double getHeuristicFactor(T candidate);

    protected EnumeratedDistribution<T> createCandidateDistribution(List<T> candidates) {
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
}
