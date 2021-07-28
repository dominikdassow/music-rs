package de.dominikdassow.musicrs.recommender.operator.ga;

import de.dominikdassow.musicrs.recommender.solution.MusicPlaylistSolution;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import java.util.List;

// TODO
public class MusicPlaylistSelection
    implements SelectionOperator<List<MusicPlaylistSolution>, MusicPlaylistSolution> {

    private final SelectionOperator<List<MusicPlaylistSolution>, MusicPlaylistSolution> selection;

    public MusicPlaylistSelection() {
        this.selection = new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());
    }

    @Override
    public MusicPlaylistSolution execute(List<MusicPlaylistSolution> solutions) {
        return selection.execute(solutions);
    }
}
