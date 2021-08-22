package de.dominikdassow.musicrs.recommender;

import de.dominikdassow.musicrs.model.SimilarTracksList;
import de.dominikdassow.musicrs.recommender.engine.SimilarTracksEngine;
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

    private final Map<Integer, String> tracks;
    // private final FixedBaseList<String> solutionTracks;
    private final List<String> candidateTracks;
    private final List<Objective> objectives;

    public MusicPlaylistContinuationProblem(
        SimilarTracksEngine similarTracksEngine,
        Map<Integer, String> tracks,
        List<SimilarTracksList> similarTracks) {

        this.tracks = tracks;
        // solutionTracks = new FixedBaseList<>(tracks);

        candidateTracks = new ArrayList<>(new HashSet<>() {{
            similarTracks.forEach(list -> addAll(list.getTracks()));
        }});

        objectives = Arrays.asList(
            new AccuracyObjective(similarTracks),
            new NoveltyObjective(similarTracks),
            new DiversityObjective(similarTracksEngine)
        );

        setNumberOfVariables(tracks.size() + candidateTracks.size());
        setNumberOfObjectives(objectives.size());
        setName("MPC");
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
        final FixedBaseList<String> solutionTracks = new FixedBaseList<>(tracks);

//        final FixedBaseList<String> solutionTracks = new FixedBaseList<>(this.solutionTracks,
//            solution.getVariables().stream().map(candidateTracks::get).collect(Collectors.toList()));

//        solutionTracks.reset();
//
        solution.getVariables()
            .forEach(index -> solutionTracks.add(candidateTracks.get(index)));

        for (int i = 0; i < objectives.size(); i++) {
            // TODO: Constant
            List<String> values = solutionTracks.values();
            if (values.size() < 500) log.info(i + " :: " + values.size());
            solution.setObjective(i, objectives.get(i).evaluate(values.subList(0, 500)));
        }
    }

    public List<String> getTrackIds(List<Integer> indexes) {
        return indexes.stream()
            .map(candidateTracks::get)
            .collect(Collectors.toList());
    }
}
