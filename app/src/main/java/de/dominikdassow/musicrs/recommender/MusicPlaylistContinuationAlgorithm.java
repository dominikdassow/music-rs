package de.dominikdassow.musicrs.recommender;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;

import java.util.List;

public interface MusicPlaylistContinuationAlgorithm {

    Algorithm<List<PermutationSolution<Integer>>> get();

    MusicPlaylistContinuationProblem getProblem();
}
