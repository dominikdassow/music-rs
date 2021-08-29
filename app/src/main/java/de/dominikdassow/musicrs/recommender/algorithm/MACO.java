package de.dominikdassow.musicrs.recommender.algorithm;

import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationAlgorithm;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationProblem;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.MACOBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;

import java.util.List;

public class MACO
    implements MusicPlaylistContinuationAlgorithm {

    private final MusicPlaylistContinuationProblem problem;

    private final Algorithm<List<PermutationSolution<Integer>>> algorithm;

    public MACO(MusicPlaylistContinuationProblem problem, Configuration configuration) {
        this.problem = problem;

        algorithm = new MACOBuilder<>(problem, configuration.getVariant().id)
            .setNumberOfAnts(configuration.getNumberOfAnts())
            .setNumberOfCycles(configuration.getNumberOfCycles())
            .setPheromoneFactorsWeight(configuration.getPheromoneFactorsWeight())
            .setHeuristicFactorsWeight(configuration.getHeuristicFactorsWeight())
            .setEvaporationFactor(configuration.getEvaporationFactor())
            .build();
    }

    @Override
    public Algorithm<List<PermutationSolution<Integer>>> get() {
        return algorithm;
    }

    @Override
    public MusicPlaylistContinuationProblem getProblem() {
        return problem;
    }

    @Builder
    @RequiredArgsConstructor
    public static
    class Configuration
        implements AlgorithmConfiguration {

        public enum Variant {
            MULTIPLE_COLONIES_MULTIPLE_RANDOM_PHEROMONE_TRAILS(1),
            MULTIPLE_COLONIES_MULTIPLE_SUMMED_PHEROMONE_TRAILS(2),
            ONE_COLONY_ONE_PHEROMONE_TRAIL(3),
            ONE_COLONY_MULTIPLE_RANDOM_PHEROMONE_TRAILS(4);

            public final int id;

            Variant(int id) {
                this.id = id;
            }
        }

        @Getter
        private final Variant variant;

        @Getter
        private final int numberOfAnts;

        @Getter
        private final int numberOfCycles;

        @Getter
        private final double pheromoneFactorsWeight;

        @Getter
        private final double heuristicFactorsWeight;

        @Getter
        private final double evaporationFactor;

        public String getName() {
            return "MACO" + variant.id
                + "__" + numberOfAnts
                + "__" + numberOfCycles
                + "__" + String.valueOf(pheromoneFactorsWeight).replace('.', '_')
                + "__" + String.valueOf(heuristicFactorsWeight).replace('.', '_')
                + "__" + String.valueOf(evaporationFactor).replace('.', '_');
        }

        @Override
        public MusicPlaylistContinuationAlgorithm createAlgorithmFor(MusicPlaylistContinuationProblem problem) {
            return new MACO(problem, this);
        }
    }
}
