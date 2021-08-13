package de.dominikdassow.musicrs.recommender;

import de.dominikdassow.musicrs.model.Playlist;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.SimilarTracksList;
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

    public MusicPlaylistContinuationProblem(Playlist playlist, List<SimilarTracksList> similarTracksLists) {
        solutionTracks = new FixedBaseList<>(playlist.getTracks().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getId())));

        candidateTracks = new ArrayList<>(new HashSet<>() {{
            similarTracksLists.forEach(playlist -> addAll(playlist.getTracks().values()));
        }});

        TracksFeaturesData tracksFeaturesData = new TracksFeaturesData(new TrackFeatureIndex(new HashSet<>() {{
            addAll(playlist.getTracks().values());
            similarTracksLists.forEach(playlist -> addAll(playlist.getTracks().values()));
        }}));

        objectives = Arrays.asList(
            new AccuracyObjective(similarTracksLists),
            new NoveltyObjective(similarTracksLists),
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
            // TODO: Constant
            solution.setObjective(i, objectives.get(i).evaluate(solutionTracks.values().subList(0, 500)));
        }
    }

    public List<Integer> getTrackIds(List<Integer> indexes) {
        return indexes.stream()
            .map(index -> candidateTracks.get(index).getId())
            .collect(Collectors.toList());
    }
}
