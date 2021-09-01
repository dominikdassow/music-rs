package de.dominikdassow.musicrs.recommender;

import de.dominikdassow.musicrs.model.SimilarTracksList;
import de.dominikdassow.musicrs.recommender.engine.SimilarTracksEngine;
import de.dominikdassow.musicrs.recommender.objective.AccuracyObjective;
import de.dominikdassow.musicrs.recommender.objective.DiversityObjective;
import de.dominikdassow.musicrs.recommender.objective.NoveltyObjective;
import de.dominikdassow.musicrs.recommender.objective.Objective;
import de.dominikdassow.musicrs.recommender.problem.GrowingProblem;
import de.dominikdassow.musicrs.recommender.solution.GrowingSolution;
import de.dominikdassow.musicrs.recommender.solution.GrowingUniqueIntegerSolution;
import de.dominikdassow.musicrs.util.FixedBaseList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uma.jmetal.problem.AbstractGenericProblem;
import org.uma.jmetal.problem.permutationproblem.PermutationProblem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;
import org.uma.jmetal.solution.permutationsolution.impl.IntegerPermutationSolution;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public abstract class MusicPlaylistContinuationProblem<S extends Solution<Integer>>
    extends AbstractGenericProblem<S> {

    protected final Map<Integer, String> tracks;
    protected final List<String> candidateTracks;
    protected final List<Objective> objectives;

    public MusicPlaylistContinuationProblem(Configuration configuration) {
        tracks = configuration.getTracks();

        candidateTracks = new ArrayList<>(new HashSet<>() {{
            configuration.getSimilarTracksLists().forEach(list -> addAll(list.getTracks()));
        }});

        objectives = Arrays.asList(
            new AccuracyObjective(configuration.getSimilarTracksLists()),
            new NoveltyObjective(configuration.getSimilarTracksLists()),
            new DiversityObjective(configuration.getSimilarTracksEngine())
        );

        setNumberOfVariables(candidateTracks.size());
        setNumberOfObjectives(objectives.size());
        setName("MPC");
    }

    @Override
    public void evaluate(S solution) {
        final FixedBaseList<String> solutionTracks = new FixedBaseList<>(tracks);

        solution.getVariables()
            .forEach(index -> solutionTracks.add(candidateTracks.get(index)));

        List<String> tracks = solutionTracks.values();

        for (int i = 0; i < objectives.size(); i++) {
            solution.setObjective(i, objectives.get(i)
                .evaluate(tracks.subList(0, Math.min(500, tracks.size())))); // TODO: Constant
        }
    }

    public List<String> getTrackIds(List<Integer> indexes) {
        return indexes.stream()
            .map(candidateTracks::get)
            .collect(Collectors.toList());
    }

    public static class Permutation
        extends MusicPlaylistContinuationProblem<PermutationSolution<Integer>>
        implements PermutationProblem<PermutationSolution<Integer>> {

        public Permutation(Configuration configuration) {
            super(configuration);
        }

        @Override
        public int getLength() {
            return getNumberOfVariables();
        }

        @Override
        public PermutationSolution<Integer> createSolution() {
            return new IntegerPermutationSolution(candidateTracks.size(), getNumberOfObjectives());
        }
    }

    public static class Growing
        extends MusicPlaylistContinuationProblem<GrowingSolution<Integer>>
        implements GrowingProblem<GrowingSolution<Integer>, Integer> {

        private final Map<Integer, Map<Integer, Double>> candidatesValues;

        public Growing(Configuration configuration) {
            super(configuration);

            candidatesValues = new ConcurrentHashMap<>(candidateTracks.size());

            IntStream.range(0, candidateTracks.size()).forEach(
                candidate -> candidatesValues.put(candidate, new ConcurrentHashMap<>(getNumberOfObjectives())));
        }

        @Override
        public List<Integer> getCandidates() {
            return List.copyOf(candidatesValues.keySet());
        }

        @Override
        public double evaluate(Integer candidate, int objective) {
            return candidatesValues.get(candidate)
                .computeIfAbsent(objective, o -> objectives.get(o).evaluate(candidateTracks.get(candidate)));
        }

        @Override
        public double applyObjectiveValueNormalization(int objective, double value) {
            return value * -1;
        }

        @Override
        public boolean isCandidateRewardedInSolution(Integer candidate, GrowingSolution<Integer> solution) {
            return solution.getVariables().indexOf(candidate) < 500; // TODO: Constant
        }

        @Override
        public GrowingSolution<Integer> createSolution() {
            return new GrowingUniqueIntegerSolution(getNumberOfVariables(), getNumberOfObjectives());
        }
    }

    @RequiredArgsConstructor
    public static class Configuration {

        @Getter
        private final SimilarTracksEngine similarTracksEngine;

        @Getter
        private final List<SimilarTracksList> similarTracksLists;

        @Getter
        private final Map<Integer, String> tracks;
    }

}
