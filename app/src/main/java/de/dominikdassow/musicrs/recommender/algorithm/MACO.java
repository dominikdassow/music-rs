package de.dominikdassow.musicrs.recommender.algorithm;

import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationAlgorithm;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationProblem;
import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.MACOBuilder;
import de.dominikdassow.musicrs.recommender.solution.GrowingSolution;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.uma.jmetal.algorithm.Algorithm;

import java.util.List;

public class MACO
    implements MusicPlaylistContinuationAlgorithm<GrowingSolution<Integer>> {

    private final MusicPlaylistContinuationProblem.Growing problem;

    private final Algorithm<List<GrowingSolution<Integer>>> algorithm;

    public MACO(MusicPlaylistContinuationProblem.Growing problem, Configuration configuration) {
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
    public Algorithm<List<GrowingSolution<Integer>>> get() {
        return algorithm;
    }

    @Override
    public MusicPlaylistContinuationProblem<GrowingSolution<Integer>> getProblem() {
        return problem;
    }

    @Builder
    @RequiredArgsConstructor
    public static
    class Configuration
        implements AlgorithmConfiguration<GrowingSolution<Integer>> {

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
        public MusicPlaylistContinuationAlgorithm<GrowingSolution<Integer>>
        createAlgorithmFor(MusicPlaylistContinuationProblem.Configuration configuration) {
            return new MACO(new MusicPlaylistContinuationProblem.Growing(configuration), this);
        }
    }
}
