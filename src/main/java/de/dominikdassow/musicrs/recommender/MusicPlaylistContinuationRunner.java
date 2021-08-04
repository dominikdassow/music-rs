package de.dominikdassow.musicrs.recommender;

import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.recommender.operator.ga.MusicPlaylistCrossover;
import de.dominikdassow.musicrs.recommender.operator.ga.MusicPlaylistMutation;
import de.dominikdassow.musicrs.recommender.operator.ga.MusicPlaylistSelection;
import de.dominikdassow.musicrs.recommender.problem.MusicPlaylistContinuationProblem;
import de.dominikdassow.musicrs.recommender.solution.MusicPlaylistSolution;
import lombok.extern.slf4j.Slf4j;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.JMetalException;

import java.util.List;

@Slf4j
public abstract class MusicPlaylistContinuationRunner
    extends AbstractAlgorithmRunner {

    protected MusicPlaylistContinuationProblem problem;

    public MusicPlaylistContinuationRunner(MusicPlaylistContinuationProblem problem) {
        this.problem = problem;
    }

    protected abstract Algorithm<List<MusicPlaylistSolution>> getAlgorithm();

    public void run() {
        Algorithm<List<MusicPlaylistSolution>> algorithm = getAlgorithm();

        long computingTime = execute(algorithm);

        List<MusicPlaylistSolution> population = algorithm.getResult();

        log.info("ALGORITHM: " + algorithm.getName());
        log.info("> TIME: " + computingTime);
        log.info("> POPULATION SIZE: " + population.size());

        List<Track> result = population.get(0).getVariables();

        log.info(">> 1st RESULT SIZE: " + result.size());
        result.forEach(track ->
            System.out.println(">>> [" + track.getId() + "] " + track.getUri() + " -> " + track.getName()));
    }

    public static class NSGAII
        extends MusicPlaylistContinuationRunner {

        private static final int POPULATION_SIZE = 10; // TODO
        private static final int MAX_EVALUATIONS = 10000; // TODO

        private static final double MUTATION_PROBABILITY = 0.25; // TODO

        private final CrossoverOperator<MusicPlaylistSolution> crossover;
        private final MutationOperator<MusicPlaylistSolution> mutation;
        private final SelectionOperator<List<MusicPlaylistSolution>, MusicPlaylistSolution> selection;

        public NSGAII(MusicPlaylistContinuationProblem problem) {
            super(problem);

            crossover = new MusicPlaylistCrossover(); // TODO
            mutation = new MusicPlaylistMutation(MUTATION_PROBABILITY);
            selection = new MusicPlaylistSelection(); // TODO
        }

        @Override
        public Algorithm<List<MusicPlaylistSolution>> getAlgorithm() {
            return new NSGAIIBuilder<>(problem, crossover, mutation, POPULATION_SIZE)
                .setSelectionOperator(selection)
                .setMaxEvaluations(MAX_EVALUATIONS)
                .build();
        }
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
}
