package de.dominikdassow.musicrs.recommender;

import de.dominikdassow.musicrs.recommender.algorithm.AlgorithmConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.JMetalException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MusicPlaylistContinuationRunner
    extends AbstractAlgorithmRunner {

    protected final MusicPlaylistContinuationAlgorithm<Solution<Integer>> algorithm;

    public MusicPlaylistContinuationRunner(MusicPlaylistContinuationAlgorithm<Solution<Integer>> algorithm) {
        this.algorithm = algorithm;
    }

    @SuppressWarnings("unchecked")
    public MusicPlaylistContinuationRunner(
        AlgorithmConfiguration<? extends Solution<Integer>> algorithmConfiguration,
        MusicPlaylistContinuationProblem.Configuration problemConfiguration
    ) {
        this((MusicPlaylistContinuationAlgorithm<Solution<Integer>>)
            algorithmConfiguration.createAlgorithmFor(problemConfiguration));
    }

    public List<List<String>> run() {
        Algorithm<List<Solution<Integer>>> algorithm = this.algorithm.get();

        long computingTime = execute(algorithm);

        List<Solution<Integer>> population = algorithm.getResult();

        log.info("ALGORITHM: " + algorithm.getName());
        log.info("> TIME: " + computingTime);
        log.info("> POPULATION SIZE: " + population.size());

        return population.stream()
            .map(solution -> this.algorithm.getProblem().getTrackIds(solution.getVariables()))
            .collect(Collectors.toList());
    }

    private static long execute(Algorithm<?> algorithm) {
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
