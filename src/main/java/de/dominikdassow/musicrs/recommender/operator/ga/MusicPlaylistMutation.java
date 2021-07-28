package de.dominikdassow.musicrs.recommender.operator.ga;

import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.recommender.solution.MusicPlaylistSolution;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.PermutationSwapMutation;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;

// TODO
public class MusicPlaylistMutation
    implements MutationOperator<MusicPlaylistSolution> {

    private final MutationOperator<PermutationSolution<Track>> mutation;

    public MusicPlaylistMutation(double mutationProbability) {
        this.mutation = new PermutationSwapMutation<>(mutationProbability);
    }

    @Override
    public double getMutationProbability() {
        return mutation.getMutationProbability();
    }

    @Override
    public MusicPlaylistSolution execute(MusicPlaylistSolution solution) {
        return (MusicPlaylistSolution) mutation.execute(solution);
    }
}
