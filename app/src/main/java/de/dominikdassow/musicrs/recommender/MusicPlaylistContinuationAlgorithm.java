package de.dominikdassow.musicrs.recommender;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;

import java.util.List;

public interface MusicPlaylistContinuationAlgorithm<S extends Solution<Integer>> {

    Algorithm<List<S>> get();

    MusicPlaylistContinuationProblem<S> getProblem();
}
