package de.dominikdassow.musicrs.recommender.algorithm;

import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationAlgorithm;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationProblem;

public interface AlgorithmConfiguration {

    String getName();

    MusicPlaylistContinuationAlgorithm createAlgorithmFor(MusicPlaylistContinuationProblem problem);
}
