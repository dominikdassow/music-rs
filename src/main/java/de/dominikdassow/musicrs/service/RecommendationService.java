package de.dominikdassow.musicrs.service;

import de.dominikdassow.musicrs.model.AnyPlaylist;
import de.dominikdassow.musicrs.model.ChallengePlaylist;
import de.dominikdassow.musicrs.model.DatasetPlaylist;
import de.dominikdassow.musicrs.recommender.engine.SimilarPlaylistsEngine;
import de.dominikdassow.musicrs.repository.ChallengeSetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@SuppressWarnings("unused")
public class RecommendationService {

    @Autowired
    private ChallengeSetRepository challengeSetRepository;

    @Autowired
    private SimilarPlaylistsEngine similarPlaylistsEngine;

    public void run() {
        List<AnyPlaylist> similarPlaylists = similarPlaylistsEngine.getResults(123, 5); // TODO

        similarPlaylists.forEach(playlist -> log.info("[" + playlist.getId() + "] " + playlist.getName()));

        // final ChallengePlaylist playlist = challengeSetRepository.findById(1000020).orElseThrow();

        // final MusicPlaylistContinuationProblem problem
        //    = new MusicPlaylistContinuationProblem(playlist, getSimilarPlaylistsFor(playlist));

        // new MusicPlaylistContinuationRunner.NSGAII(problem).run();
    }

    private List<DatasetPlaylist> getSimilarPlaylistsFor(ChallengePlaylist playlist) {
        return new ArrayList<>(); // TODO
    }
}
