package de.dominikdassow.musicrs.recommender.algorithm;

import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationAlgorithm;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationProblem;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class NOOP
    implements MusicPlaylistContinuationAlgorithm<PermutationSolution<Integer>> {

    private final MusicPlaylistContinuationProblem.Permutation problem;

    private final Algorithm<List<PermutationSolution<Integer>>> algorithm;

    public NOOP(MusicPlaylistContinuationProblem.Permutation problem, Configuration configuration) {
        this.problem = problem;

        algorithm = new Algorithm<>() {
            private final SolutionListEvaluator<PermutationSolution<Integer>> evaluator
                = new SequentialSolutionListEvaluator<>();

            private List<PermutationSolution<Integer>> population;

            @Override
            public void run() {
                population = new ArrayList<>(configuration.getPopulationSize()) {{
                    IntStream.range(0, configuration.getPopulationSize())
                        .forEach(i -> add(getProblem().createSolution()));
                }};

                population = evaluator.evaluate(population, problem);
            }

            @Override
            public List<PermutationSolution<Integer>> getResult() {
                return SolutionListUtils.getNonDominatedSolutions(population);
            }

            @Override
            public String getName() {
                return "NOOP";
            }

            @Override
            public String getDescription() {
                return "No-Operation algorithm";
            }
        };
    }

    @Override
    public Algorithm<List<PermutationSolution<Integer>>> get() {
        return algorithm;
    }

    @Override
    public MusicPlaylistContinuationProblem<PermutationSolution<Integer>> getProblem() {
        return problem;
    }

    @Builder
    @RequiredArgsConstructor
    public static
    class Configuration
        implements AlgorithmConfiguration<PermutationSolution<Integer>> {

        @Getter
        private final int populationSize;

        public String getName() {
            return "NOOP"
                + "__" + populationSize;
        }

        @Override
        public MusicPlaylistContinuationAlgorithm<PermutationSolution<Integer>>
        createAlgorithmFor(MusicPlaylistContinuationProblem.Configuration configuration) {
            return new NOOP(new MusicPlaylistContinuationProblem.Permutation(configuration), this);
        }
    }
}
