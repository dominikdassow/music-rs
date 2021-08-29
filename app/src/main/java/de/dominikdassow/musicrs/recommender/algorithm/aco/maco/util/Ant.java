package de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.MACO;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.tuple.Tuple2;
import org.uma.jmetal.solution.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class Ant<S extends Solution<T>, T> {

    private final MACO<S, T> algorithm;
    private final Colony<S, T> colony;

    public Ant(MACO<S, T> algorithm, Colony<S, T> colony) {
        this.algorithm = algorithm;
        this.colony = colony;
    }

    public S createSolution() {
        S solution = algorithm.getProblem().createSolution();

        algorithm.getProblem().evaluate(solution);

        CandidateDistribution<T> candidateDistribution = createCandidateDistribution(solution);
        List<T> candidates = new ArrayList<>();

        for (int i = 0; i < solution.getNumberOfVariables(); i++) {
            candidates.add(candidateDistribution.getNext(candidates));
        }

        for (int i = 0; i < solution.getNumberOfVariables(); i++) {
            solution.setVariable(i, candidates.get(i));
        }

        return solution;
    }

    private CandidateDistribution<T> createCandidateDistribution(S solution) {
        Map<T, Double> pheromoneFactors = colony.getPheromoneFactors(solution);
        Map<T, Double> heuristicFactors = colony.getHeuristicFactors(solution);

        double summedCandidatesFactor = solution.getVariables().stream()
            .mapToDouble(candidate -> pheromoneFactors.get(candidate) * heuristicFactors.get(candidate))
            .sum();

        Map<T, Double> probabilities = solution.getVariables().stream()
            .collect(Collectors.toMap(Function.identity(),
                candidate -> (pheromoneFactors.get(candidate) * heuristicFactors.get(candidate))
                    / summedCandidatesFactor));

        List<Tuple2<T, Double>> candidatesWithProbability = solution.getVariables().stream()
            .map(candidate -> new Tuple2<>(candidate, probabilities.get(candidate)))
            .collect(Collectors.toList());

        return new CandidateDistribution<>(candidatesWithProbability);
    }
}
