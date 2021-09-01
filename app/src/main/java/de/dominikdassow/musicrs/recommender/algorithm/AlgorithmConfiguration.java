package de.dominikdassow.musicrs.recommender.algorithm;

import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationAlgorithm;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationProblem;
import org.uma.jmetal.solution.Solution;

public interface AlgorithmConfiguration<S extends Solution<Integer>> {

    static AlgorithmConfiguration<? extends Solution<Integer>> fromName(String name) {
        String algorithm = name.split("__")[0];

        switch (algorithm) {
            case "NOOP":
                return NOOP.Configuration.fromName(name);
            case "NSGAII":
                return NSGAII.Configuration.fromName(name);
            case "SMSEMOA":
                return SMSEMOA.Configuration.fromName(name);
            case "MACO1":
            case "MACO2":
            case "MACO3":
            case "MACO4":
                return MACO.Configuration.fromName(name);
            default:
                throw new IllegalStateException("Unexpected value: " + algorithm);
        }
    }

    String getName();

    MusicPlaylistContinuationAlgorithm<S>
    createAlgorithmFor(MusicPlaylistContinuationProblem.Configuration configuration);
}
