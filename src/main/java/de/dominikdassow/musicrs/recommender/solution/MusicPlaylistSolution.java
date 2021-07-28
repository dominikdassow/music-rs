package de.dominikdassow.musicrs.recommender.solution;

import de.dominikdassow.musicrs.model.Track;
import org.uma.jmetal.solution.AbstractSolution;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;

import java.util.HashMap;
import java.util.List;

public class MusicPlaylistSolution
    extends AbstractSolution<Track>
    implements PermutationSolution<Track> {

    public MusicPlaylistSolution(List<Track> tracks, int numberOfObjectives) {
        super(tracks.size(), numberOfObjectives);

        for (int i = 0; i < tracks.size(); i++) {
            getVariables().set(i, tracks.get(i));
        }
    }

    public MusicPlaylistSolution(MusicPlaylistSolution solution) {
        super(solution.getLength(), solution.getObjectives().length);

        for (int i = 0; i < getObjectives().length; i++) {
            getObjectives()[i] = solution.getObjectives()[i];
        }

        for (int i = 0; i < getVariables().size(); i++) {
            getVariables().set(i, solution.getVariables().get(i));
        }

        for (int i = 0; i < getConstraints().length; i++) {
            getConstraints()[i] = solution.getConstraints()[i];
        }

        attributes = new HashMap<>(solution.attributes);
    }

    @Override
    public int getLength() {
        return getVariables().size();
    }

    @Override
    public MusicPlaylistSolution copy() {
        return new MusicPlaylistSolution(this);
    }
}
