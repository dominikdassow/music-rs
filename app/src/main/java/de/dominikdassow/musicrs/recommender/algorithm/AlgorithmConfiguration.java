package de.dominikdassow.musicrs.recommender.algorithm;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

public interface AlgorithmConfiguration {

    @Builder
    @ToString(includeFieldNames = false)
    @RequiredArgsConstructor
    class NSGAII
        implements AlgorithmConfiguration {;

        @Getter
        private final int populationSize;

        @Getter
        private final int maxEvaluations;

        @Getter
        private final double crossoverProbability;

        @Getter
        private final double mutationProbability;
    }
}
