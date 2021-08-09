package de.dominikdassow.musicrs.service;

import de.dominikdassow.musicrs.model.DatasetPlaylist;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.playlist.SimilarPlaylist;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationEvaluator;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationRunner;
import de.dominikdassow.musicrs.recommender.engine.SimilarPlaylistsEngine;
import de.dominikdassow.musicrs.recommender.problem.MusicPlaylistContinuationProblem;
import de.dominikdassow.musicrs.repository.DatasetRepository;
import de.dominikdassow.musicrs.repository.TrackRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@Component
@Slf4j
@SuppressWarnings("unused")
public class EvaluationService {

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private SimilarPlaylistsEngine similarPlaylistsEngine;

    public void run() {
        final DatasetPlaylist playlist
            = datasetRepository.findById(90).orElseThrow();

        playlist.setTracks(new HashMap<>() {{
            IntStream.range(0, 25)
                .forEach(i -> put(i, playlist.getTracks().get(String.valueOf(i)))); // TODO: Conversion issue
        }});

        final List<Track> trackList = playlist.getTrackList();

        similarPlaylistsEngine.init(List.of(playlist)); // TODO

        final List<SimilarPlaylist> similarPlaylists
            = similarPlaylistsEngine.getResults(playlist, 500);

        log.info("# SIMILAR PLAYLISTS: " + similarPlaylists.size());
        log.info(similarPlaylists.stream().map(SimilarPlaylist::getId).collect(Collectors.toList()).toString());

        final MusicPlaylistContinuationProblem problem
            = new MusicPlaylistContinuationProblem(playlist, similarPlaylists);

        List<MusicPlaylistContinuationRunner> runners = List.of(
            new MusicPlaylistContinuationRunner.NSGAII(problem)
        );

        runners.forEach(runner -> {
            List<List<Integer>> results = runner.run();

            Map<Integer, Track> allTracksById = StreamSupport
                .stream(trackRepository.findAllById(results.stream()
                    .flatMap(Collection::stream).collect(Collectors.toSet())).spliterator(), false)
                .collect(Collectors.toMap(Track::getId, Function.identity()));

            IntStream.range(0, results.size()).forEach(i -> {
                List<Track> tracks = results.get(i).stream()
                    .map(allTracksById::get)
                    .collect(Collectors.toList());

                log.info("### RESULT " + i + " [" + tracks.size() + "]");

                log.info("(1) " + tracks.get(0).getUri() + " [" + tracks.get(0).getId() + "]");
                log.info("(2) " + tracks.get(1).getUri() + " [" + tracks.get(1).getId() + "]");
                log.info("(3) " + tracks.get(2).getUri() + " [" + tracks.get(2).getId() + "]");

                final MusicPlaylistContinuationEvaluator evaluator
                    = new MusicPlaylistContinuationEvaluator(trackList, tracks.subList(0, 500));

                log.info("> R-Precision: " + evaluator.getRPrecision());
                log.info("> NDCG: " + evaluator.getNDCG());
                log.info("> Recommended Songs Clicks: " + evaluator.getRecommendedSongsClicks());

                log.info("###");
            });
        });
    }
}
