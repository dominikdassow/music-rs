package de.dominikdassow.musicrs.service;

import de.dominikdassow.musicrs.AppConfiguration;
import de.dominikdassow.musicrs.model.Playlist;
import de.dominikdassow.musicrs.model.SimilarTracksList;
import de.dominikdassow.musicrs.model.feature.PlaylistFeature;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationProblem;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationRunner;
import de.dominikdassow.musicrs.recommender.algorithm.AlgorithmConfiguration;
import de.dominikdassow.musicrs.recommender.engine.SimilarPlaylistsEngine;
import de.dominikdassow.musicrs.recommender.engine.SimilarTracksEngine;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;
import org.uma.jmetal.solution.Solution;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class RecommendationService {

    private final SimilarPlaylistsEngine similarPlaylistsEngine;
    private final SimilarTracksEngine similarTracksEngine;

    public RecommendationService() {
        similarPlaylistsEngine = new SimilarPlaylistsEngine();
        similarTracksEngine = new SimilarTracksEngine();
    }

    public RecommendationService(Collection<Playlist.Sample> samples) {
        Collection<String> excludedIds = samples.stream()
            .map(Playlist.Sample::getId)
            .map(String::valueOf)
            .collect(Collectors.toList());

        similarPlaylistsEngine = new SimilarPlaylistsEngine() {
            @Override
            public boolean filterPlaylistIndex(String[] data) {
                return !excludedIds.contains(data[0]);
            }

            @Override
            public boolean filterPlaylistFeatureIndex(String[] data) {
                return !excludedIds.contains(data[0]);
            }

            @Override
            public Stream<Integer> additionalPlaylists() {
                return samples.stream()
                    .map(Playlist.Sample::getId);
            }

            @Override
            public Stream<String> additionalPlaylistFeatures() {
                return samples.stream()
                    .flatMap(sample -> sample.getFeatures().stream().map(PlaylistFeature::getId));
            }

            @Override
            public Stream<Tuple3<Integer, String, Double>> additionalPlaylistFeatureValues() {
                return new ArrayList<Tuple3<Integer, String, Double>>() {{
                    samples.forEach(sample ->
                        sample.getFeatures().forEach(feature ->
                            add(Tuple.tuple(sample.getId(), feature.getId(), feature.getValue()))));
                }}.stream();
            }
        };

        similarTracksEngine = new SimilarTracksEngine();
    }

    public List<Result> makeRecommendations(
        Set<Integer> playlists,
        List<AlgorithmConfiguration<? extends Solution<Integer>>> algorithmConfigurations
    ) {
        List<Result> recommendations = new ArrayList<>();

        playlists.forEach(playlist -> {
            Map<Integer, String> tracks
                = DatabaseService.readPlaylistTracks(playlist);

            List<SimilarTracksList> similarTracksLists
                = DatabaseService.readSimilarTracksLists(playlist);

            long numberOfUniqueTracks = similarTracksLists.stream()
                .flatMap(similarTracksList -> similarTracksList.getTracks().stream())
                .distinct()
                .count();

            if (numberOfUniqueTracks < AppConfiguration.get().numberOfTracks) {
                log.trace("# [" + playlist + "] SIMILAR PLAYLISTS :: GENERATE");

                if (tracks.isEmpty()) {
                    similarTracksLists = similarPlaylistsEngine
                        .getRandomSimilarTracksFor(playlist);
                } else {
                    similarTracksLists = similarPlaylistsEngine
                        .getSimilarTracksFor(playlist);
                }
            }

            log.info("# [" + playlist + "] SIMILAR PLAYLISTS :: "
                + similarTracksLists.size() + " :: "
                + similarTracksLists.stream()
                .flatMap(similarTracksList -> similarTracksList.getTracks().stream())
                .distinct()
                .count());

            MusicPlaylistContinuationProblem.Configuration problemConfiguration
                = new MusicPlaylistContinuationProblem.Configuration(similarTracksEngine, similarTracksLists, tracks);

            Map<AlgorithmConfiguration<? extends Solution<Integer>>, MusicPlaylistContinuationRunner> runners =
                new HashMap<>() {{
                    algorithmConfigurations.forEach(configuration -> put(configuration,
                        new MusicPlaylistContinuationRunner(configuration, problemConfiguration)));
                }};

            runners.forEach((configuration, runner) -> {
                log.info("RUN: " + configuration.getName());

                List<List<String>> results = runner.run();

                recommendations.add(new Result(playlist, configuration, results));
            });
        });

        return recommendations;
    }

    @RequiredArgsConstructor
    public static class Result {

        @Getter
        private final Integer playlist;

        @Getter
        private final AlgorithmConfiguration<? extends Solution<Integer>> configuration;

        @Getter
        private final List<List<String>> tracks;
    }
}
