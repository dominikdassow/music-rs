package de.dominikdassow.musicrs.recommender;

import lombok.extern.slf4j.Slf4j;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.JMetalException;

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

}
