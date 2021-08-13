package de.dominikdassow.musicrs.service;

import de.dominikdassow.musicrs.model.Playlist;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.SimilarTracksList;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationRunner;
import de.dominikdassow.musicrs.recommender.SimilarPlaylistsEngine;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationProblem;
import de.dominikdassow.musicrs.repository.TrackRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Slf4j
@SuppressWarnings("unused")
public class RecommendationService {

    public enum AlgorithmType {
        NSGAII
    }

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private SimilarPlaylistsEngine similarPlaylistsEngine;

    public List<Result> makeRecommendationsFor(
        Set<Playlist> playlists,
        Set<AlgorithmType> algorithms
    ) {
        return makeRecommendations(playlists, Set.of(), algorithms);
    }

    public List<Result> makeSampledRecommendations(
        Set<Playlist> playlists,
        Set<AlgorithmType> algorithms
    ) {
        return makeRecommendations(playlists, playlists, algorithms);
    }

    private List<Result> makeRecommendations(
        Set<Playlist> playlists,
        Set<Playlist> excluded,
        Set<AlgorithmType> algorithms
    ) {
        similarPlaylistsEngine.init(excluded); // TODO

        List<Result> recommendations = new ArrayList<>();

        playlists.forEach(playlist -> {
            final List<SimilarTracksList> similarTracksLists
                = similarPlaylistsEngine.getResults(playlist, 500); // TODO: Constant

            log.info("# SIMILAR PLAYLISTS: " + similarTracksLists.size());

            final MusicPlaylistContinuationProblem problem
                = new MusicPlaylistContinuationProblem(playlist, similarTracksLists);

            Map<AlgorithmType, MusicPlaylistContinuationRunner> runners = new HashMap<>() {{
                if (algorithms.contains(AlgorithmType.NSGAII)) {
                    put(AlgorithmType.NSGAII, new MusicPlaylistContinuationRunner.NSGAII(problem));
                }
            }};

            runners.forEach((algorithm, runner) -> {
                log.info("RUN: " + algorithm);

                List<List<Integer>> results = runner.run();

                Map<Integer, Track> allTracksById = StreamSupport
                    .stream(trackRepository.findAllById(results.stream()
                        .flatMap(Collection::stream).collect(Collectors.toSet())).spliterator(), false)
                    .collect(Collectors.toMap(Track::getId, Function.identity()));

                recommendations.add(new Result(playlist, algorithm, results.stream()
                    .map(r -> r.stream().map(allTracksById::get).collect(Collectors.toList()))
                    .collect(Collectors.toList())));
            });
        });

        return recommendations;
    }

    @Data
    @AllArgsConstructor
    public static class Result {

        private Playlist playlist;

        private AlgorithmType algorithm;

        private List<List<Track>> tracks;
    }
}
