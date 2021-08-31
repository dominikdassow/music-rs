package de.dominikdassow.musicrs.recommender.algorithm;

import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationAlgorithm;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationProblem;
import org.uma.jmetal.solution.Solution;

public interface AlgorithmConfiguration<S extends Solution<Integer>> {

    String getName();

    MusicPlaylistContinuationAlgorithm<S>
    createAlgorithmFor(MusicPlaylistContinuationProblem.Configuration configuration);
}
