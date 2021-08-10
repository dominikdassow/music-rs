package de.dominikdassow.musicrs.recommender.problem;

import de.dominikdassow.musicrs.model.AnyPlaylist;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.playlist.SimilarPlaylist;
import de.dominikdassow.musicrs.recommender.data.TracksFeaturesData;
import de.dominikdassow.musicrs.recommender.index.TrackFeatureIndex;
import de.dominikdassow.musicrs.recommender.objective.AccuracyObjective;
import de.dominikdassow.musicrs.recommender.objective.DiversityObjective;
import de.dominikdassow.musicrs.recommender.objective.NoveltyObjective;
import de.dominikdassow.musicrs.recommender.objective.Objective;
import de.dominikdassow.musicrs.util.FixedBaseList;
import lombok.extern.slf4j.Slf4j;
import org.uma.jmetal.problem.AbstractGenericProblem;
import org.uma.jmetal.problem.permutationproblem.PermutationProblem;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;
import org.uma.jmetal.solution.permutationsolution.impl.IntegerPermutationSolution;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class MusicPlaylistContinuationProblem
    extends AbstractGenericProblem<PermutationSolution<Integer>>
    implements PermutationProblem<PermutationSolution<Integer>> {

    private final FixedBaseList<Integer> solutionTracks;
    private final List<Track> candidateTracks;

    private final List<Objective> objectives;

    public MusicPlaylistContinuationProblem(AnyPlaylist playlist, List<SimilarPlaylist> similarPlaylists) {
        solutionTracks = new FixedBaseList<>(new HashMap<>() {{
            for (Map.Entry<Integer, Track> entry : playlist.getTracks().entrySet()) {
                put(Integer.valueOf(String.valueOf(entry.getKey())), entry.getValue().getId());
            }
        }});

        candidateTracks = new ArrayList<>(new HashSet<>() {{
            similarPlaylists.forEach(playlist -> addAll(playlist.getTracks().values()));
        }});

        TracksFeaturesData tracksFeaturesData = new TracksFeaturesData(new TrackFeatureIndex(new HashSet<>() {{
            addAll(playlist.getTracks().values());
            similarPlaylists.forEach(playlist -> addAll(playlist.getTracks().values()));
        }}));

        objectives = Arrays.asList(
            new AccuracyObjective(similarPlaylists),
            new NoveltyObjective(similarPlaylists),
            new DiversityObjective(tracksFeaturesData)
        );

        setNumberOfVariables(playlist.getTracks().size() + candidateTracks.size());
        setNumberOfObjectives(objectives.size());
        setName("MusicPlaylistContinuationProblem");
    }

    @Override
    public int getLength() {
        return getNumberOfVariables();
    }

    @Override
    public PermutationSolution<Integer> createSolution() {
        return new IntegerPermutationSolution(candidateTracks.size(), getNumberOfObjectives());
    }

    @Override
    public void evaluate(PermutationSolution<Integer> solution) {
        solutionTracks.reset();

        solution.getVariables()
            .forEach(index -> solutionTracks.add(candidateTracks.get(index).getId()));

        for (int i = 0; i < objectives.size(); i++) {
            solution.setObjective(i, objectives.get(i).evaluate(solutionTracks.values().subList(0, 500))); // TODO
        }
    }

    public List<Integer> getTrackIds(List<Integer> indexes) {
        return indexes.stream()
            .map(index -> candidateTracks.get(index).getId())
            .collect(Collectors.toList());
    }
}
