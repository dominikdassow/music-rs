package de.dominikdassow.musicrs.recommender.problem;

import de.dominikdassow.musicrs.model.ChallengePlaylist;
import de.dominikdassow.musicrs.model.DatasetPlaylist;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.recommender.objective.AccuracyObjective;
import de.dominikdassow.musicrs.recommender.objective.DiversityObjective;
import de.dominikdassow.musicrs.recommender.objective.Objective;
import de.dominikdassow.musicrs.recommender.objective.NoveltyObjective;
import de.dominikdassow.musicrs.recommender.solution.MusicPlaylistSolution;
import org.uma.jmetal.problem.AbstractGenericProblem;
import org.uma.jmetal.problem.permutationproblem.PermutationProblem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MusicPlaylistContinuationProblem
    extends AbstractGenericProblem<MusicPlaylistSolution>
    implements PermutationProblem<MusicPlaylistSolution> {

    private final ChallengePlaylist playlist;
    private final List<DatasetPlaylist> similarPlaylists;

    private final List<Objective> objectives;

    private List<Track> candidateTracks;

    public MusicPlaylistContinuationProblem(ChallengePlaylist playlist, List<DatasetPlaylist> similarPlaylists) {
        this.playlist = playlist;
        this.similarPlaylists = similarPlaylists;

        objectives = Arrays.asList(
            new AccuracyObjective(similarPlaylists),
            new NoveltyObjective(similarPlaylists),
            new DiversityObjective()
        );

        setNumberOfVariables(playlist.getNumberOfSamples() + getCandidateTracks().size());
        setNumberOfObjectives(objectives.size());
        setName("MusicPlaylistContinuationProblem");
    }

    @Override
    public int getLength() {
        return getNumberOfVariables();
    }

    @Override
    public MusicPlaylistSolution createSolution() {
        List<Track> tracks = new ArrayList<>() {{
            addAll(getCandidateTracks());
        }};

        java.util.Collections.shuffle(tracks);

        return new MusicPlaylistSolution(tracks, getNumberOfObjectives());
    }

    @Override
    public void evaluate(MusicPlaylistSolution solution) {
        List<Track> tracks = new ArrayList<>() {{
            addAll(playlist.getTracks().values());
            addAll(solution.getVariables());
        }};

        for (int i = 0; i < objectives.size(); i++) {
            solution.setObjective(i, objectives.get(i).evaluate(tracks));
        }
    }

    private List<Track> getCandidateTracks() {
        if (candidateTracks == null) {
            candidateTracks = new ArrayList<>(new HashSet<>() {{
                similarPlaylists.forEach(playlist -> addAll(playlist.getTracks().values()));
            }});
        }

        return candidateTracks;
    }
}
