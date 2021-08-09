package de.dominikdassow.musicrs.service;

import de.dominikdassow.musicrs.model.ChallengePlaylist;
import de.dominikdassow.musicrs.model.DatasetPlaylist;
import de.dominikdassow.musicrs.model.playlist.SimilarPlaylist;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationRunner;
import de.dominikdassow.musicrs.recommender.engine.SimilarPlaylistsEngine;
import de.dominikdassow.musicrs.recommender.problem.MusicPlaylistContinuationProblem;
import de.dominikdassow.musicrs.repository.ChallengeSetRepository;
import de.dominikdassow.musicrs.repository.DatasetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@SuppressWarnings("unused")
public class RecommendationService {

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private ChallengeSetRepository challengeSetRepository;

    @Autowired
    private SimilarPlaylistsEngine similarPlaylistsEngine;

    public void run() {
        final DatasetPlaylist playlist
            = datasetRepository.findById(90).orElseThrow();

        similarPlaylistsEngine.init(List.of(playlist)); // TODO

        final List<SimilarPlaylist> similarPlaylists
            = similarPlaylistsEngine.getResults(playlist, 500);

        log.info("# SIMILAR PLAYLISTS: " + similarPlaylists.size());

        final MusicPlaylistContinuationProblem problem
            = new MusicPlaylistContinuationProblem(playlist, similarPlaylists);

        new MusicPlaylistContinuationRunner.NSGAII(problem).run();
    }
}
