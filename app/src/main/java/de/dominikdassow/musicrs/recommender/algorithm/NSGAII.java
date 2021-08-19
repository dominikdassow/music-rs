package de.dominikdassow.musicrs.recommender.algorithm;

import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationProblem;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationRunner;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.PMXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.PermutationSwapMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import java.util.List;

public class NSGAII
    extends MusicPlaylistContinuationRunner {

    private final AlgorithmConfiguration.NSGAII configuration;

    public NSGAII(MusicPlaylistContinuationProblem problem, AlgorithmConfiguration.NSGAII configuration) {
        super(problem);

        this.configuration = configuration;
    }

    @Override
    public Algorithm<List<PermutationSolution<Integer>>> getAlgorithm() {
        final CrossoverOperator<PermutationSolution<Integer>> crossover
            = new PMXCrossover(configuration.getCrossoverProbability());

        final MutationOperator<PermutationSolution<Integer>> mutation
            = new PermutationSwapMutation<>(configuration.getMutationProbability());

        final SelectionOperator<List<PermutationSolution<Integer>>, PermutationSolution<Integer>> selection
            = new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());

        return new NSGAIIBuilder<>(problem, crossover, mutation, configuration.getPopulationSize())
            .setSelectionOperator(selection)
            .setMaxEvaluations(configuration.getMaxEvaluations())
            .build();
    }
}
