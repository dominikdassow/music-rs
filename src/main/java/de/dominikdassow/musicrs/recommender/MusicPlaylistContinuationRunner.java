package de.dominikdassow.musicrs.recommender;

import lombok.extern.slf4j.Slf4j;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.PMXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.PermutationSwapMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class MusicPlaylistContinuationRunner
    extends AbstractAlgorithmRunner {

    protected MusicPlaylistContinuationProblem problem;

    public MusicPlaylistContinuationRunner(MusicPlaylistContinuationProblem problem) {
        this.problem = problem;
    }

    protected abstract Algorithm<List<PermutationSolution<Integer>>> getAlgorithm();

    public List<List<String>> run() {
        Algorithm<List<PermutationSolution<Integer>>> algorithm = getAlgorithm();

        long computingTime = execute(algorithm);

        List<PermutationSolution<Integer>> population = algorithm.getResult();

        log.info("ALGORITHM: " + algorithm.getName());
        log.info("> TIME: " + computingTime);
        log.info("> POPULATION SIZE: " + population.size());

        return population.stream()
            .map(solution -> problem.getTrackIds(solution.getVariables()))
            .collect(Collectors.toList());
    }

    private long execute(Algorithm<?> algorithm) {
        long start = System.currentTimeMillis();

        Thread thread = new Thread(algorithm);
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new JMetalException("Error in thread.join()", e);
        }

        return System.currentTimeMillis() - start;
    }

    public static class NSGAII
        extends MusicPlaylistContinuationRunner {

        private static final int POPULATION_SIZE = 100; // TODO
        private static final int MAX_EVALUATIONS = 5_000; // TODO

        private static final double CROSSOVER_PROBABILITY = 0.9; // TODO
        private static final double MUTATION_PROBABILITY = 0.2; // TODO

        private final CrossoverOperator<PermutationSolution<Integer>> crossover;
        private final MutationOperator<PermutationSolution<Integer>> mutation;
        private final SelectionOperator<List<PermutationSolution<Integer>>, PermutationSolution<Integer>> selection;

        public NSGAII(MusicPlaylistContinuationProblem problem) {
            super(problem);

            crossover = new PMXCrossover(CROSSOVER_PROBABILITY);
            mutation = new PermutationSwapMutation<>(MUTATION_PROBABILITY);
            selection = new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());
        }

        @Override
        public Algorithm<List<PermutationSolution<Integer>>> getAlgorithm() {
            return new NSGAIIBuilder<>(problem, crossover, mutation, POPULATION_SIZE)
                .setSelectionOperator(selection)
                .setMaxEvaluations(MAX_EVALUATIONS)
                .build();
        }
    }
}
