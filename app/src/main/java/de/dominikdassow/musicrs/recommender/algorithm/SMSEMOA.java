package de.dominikdassow.musicrs.recommender.algorithm;

import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationAlgorithm;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationProblem;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.smsemoa.SMSEMOABuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.PMXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.PermutationSwapMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import java.util.List;

public class SMSEMOA
    implements MusicPlaylistContinuationAlgorithm {

    private final MusicPlaylistContinuationProblem problem;

    private final Algorithm<List<PermutationSolution<Integer>>> algorithm;

    public SMSEMOA(MusicPlaylistContinuationProblem problem, Configuration configuration) {
        this.problem = problem;

        final CrossoverOperator<PermutationSolution<Integer>> crossover
            = new PMXCrossover(configuration.getCrossoverProbability());

        final MutationOperator<PermutationSolution<Integer>> mutation
            = new PermutationSwapMutation<>(configuration.getMutationProbability());

        final SelectionOperator<List<PermutationSolution<Integer>>, PermutationSolution<Integer>> selection
            = new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());

        algorithm = new SMSEMOABuilder<>(problem, crossover, mutation)
            .setPopulationSize(configuration.getPopulationSize())
            .setSelectionOperator(selection)
            .setMaxEvaluations(configuration.getMaxEvaluations())
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

        @Getter
        private final int populationSize;

        @Getter
        private final int maxEvaluations;

        @Getter
        private final double crossoverProbability;

        @Getter
        private final double mutationProbability;

        public String getName() {
            return "SMSEMOA"
                + "__" + populationSize
                + "__" + maxEvaluations
                + "__" + String.valueOf(crossoverProbability).replace('.', '_')
                + "__" + String.valueOf(mutationProbability).replace('.', '_');
        }

        @Override
        public MusicPlaylistContinuationAlgorithm createAlgorithmFor(MusicPlaylistContinuationProblem problem) {
            return new SMSEMOA(problem, this);
        }
    }
}
