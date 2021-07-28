package de.dominikdassow.musicrs.recommender.operator.ga;

import de.dominikdassow.musicrs.recommender.solution.MusicPlaylistSolution;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.NullCrossover;

import java.util.List;

// TODO
public class MusicPlaylistCrossover
    implements CrossoverOperator<MusicPlaylistSolution> {

    private final CrossoverOperator<MusicPlaylistSolution> crossover;

    public MusicPlaylistCrossover() {
        this.crossover = new NullCrossover<>();
    }

    @Override
    public double getCrossoverProbability() {
        return crossover.getCrossoverProbability();
    }

    @Override
    public int getNumberOfRequiredParents() {
        return crossover.getNumberOfRequiredParents();
    }

    @Override
    public int getNumberOfGeneratedChildren() {
        return crossover.getNumberOfGeneratedChildren();
    }

    @Override
    public List<MusicPlaylistSolution> execute(List<MusicPlaylistSolution> solutions) {
        return crossover.execute(solutions);
    }
}
