package de.dominikdassow.musicrs.recommender.problem;

import de.dominikdassow.musicrs.model.ChallengePlaylist;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.playlist.SimilarPlaylist;
import de.dominikdassow.musicrs.recommender.objective.AccuracyObjective;
import de.dominikdassow.musicrs.recommender.objective.DiversityObjective;
import de.dominikdassow.musicrs.recommender.objective.NoveltyObjective;
import de.dominikdassow.musicrs.recommender.objective.Objective;
import de.dominikdassow.musicrs.recommender.solution.MusicPlaylistSolution;
import de.dominikdassow.musicrs.util.FixedBaseList;
import lombok.extern.slf4j.Slf4j;
import org.uma.jmetal.problem.AbstractGenericProblem;
import org.uma.jmetal.problem.permutationproblem.PermutationProblem;

import java.util.*;

@Slf4j
public class MusicPlaylistContinuationProblem
    extends AbstractGenericProblem<MusicPlaylistSolution>
    implements PermutationProblem<MusicPlaylistSolution> {

    private final List<Objective> objectives;

    private final FixedBaseList<Track> solutionTracks;
    private final List<Track> candidateTracks;

    public MusicPlaylistContinuationProblem(ChallengePlaylist playlist, List<SimilarPlaylist> similarPlaylists) {
        objectives = Arrays.asList(
            new AccuracyObjective(similarPlaylists),
            new NoveltyObjective(similarPlaylists),
            new DiversityObjective()
        );

        solutionTracks = new FixedBaseList<>(new HashMap<>() {{
            for (Map.Entry<Integer, Track> entry : playlist.getTracks().entrySet()) {
                put(Integer.valueOf(String.valueOf(entry.getKey())), entry.getValue());
            }
        }});

        candidateTracks = new ArrayList<>(new HashSet<>() {{
            similarPlaylists.forEach(playlist -> addAll(playlist.getTracks().values()));
        }});

        setNumberOfVariables(playlist.getNumberOfSamples() + candidateTracks.size());
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
            addAll(candidateTracks);
        }};

        java.util.Collections.shuffle(tracks);

        return new MusicPlaylistSolution(tracks, getNumberOfObjectives());
    }

    @Override
    public void evaluate(MusicPlaylistSolution solution) {
        solutionTracks.reset();

        solution.getVariables().forEach(solutionTracks::add);

        for (int i = 0; i < objectives.size(); i++) {
            solution.setObjective(i, objectives.get(i).evaluate(solutionTracks.values()));
        }
    }
}
