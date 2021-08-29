package de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util;

import org.jooq.lambda.tuple.Tuple2;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.List;
import java.util.stream.Collectors;

public class CandidateDistribution<T> {

    private final List<Tuple2<T, Double>> candidates;

    public CandidateDistribution(List<Tuple2<T, Double>> candidates) {
        this.candidates = candidates;
    }

    /**
     * Selects a candidate using roulette wheel selection,
     * where the probability of an candidate being chosen is proportional to its distribution value.
     *
     * @param seen a list of seen candidates
     * @return a new candidate that has not been seen
     * @see "https://or.stackexchange.com/a/5763"
     */
    public T getNext(List<T> seen) {
        List<Tuple2<T, Double>> remaining = candidates.stream()
            .filter(candidate -> !seen.contains(candidate.v1))
            .collect(Collectors.toList());

        double sum = remaining.stream()
            .mapToDouble(Tuple2::v2)
            .sum();

        double[] probabilities = remaining.stream()
            .mapToDouble(Tuple2::v2)
            .map(x -> x / sum)
            .toArray();

        double r = JMetalRandom.getInstance().nextDouble();

        for (int i = 0; i < probabilities.length; i++) {
            r -= probabilities[i];
            if (r < 0) return remaining.get(i).v1;
        }

        // We should never reach this point, but to appease the compiler we need a return value here.
        // We simply choose a random remaining candidate.
        return remaining.get(JMetalRandom.getInstance().nextInt(0, remaining.size() - 1)).v1;
    }
}
