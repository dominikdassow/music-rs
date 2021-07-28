package de.dominikdassow.musicrs.service;

import de.dominikdassow.musicrs.model.ChallengePlaylist;
import de.dominikdassow.musicrs.model.DatasetPlaylist;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationRunner;
import de.dominikdassow.musicrs.recommender.problem.MusicPlaylistContinuationProblem;
import de.dominikdassow.musicrs.repository.ChallengeSetRepository;
import de.dominikdassow.musicrs.repository.DatasetRepository;
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
    private DatasetRepository datasetRepository;

    @Autowired
    private ChallengeSetRepository challengeSetRepository;

    public void run() {
        final ChallengePlaylist playlist = challengeSetRepository.findById(1000020).orElseThrow();

        final MusicPlaylistContinuationProblem problem
            = new MusicPlaylistContinuationProblem(playlist, getSimilarPlaylistsFor(playlist));

        new MusicPlaylistContinuationRunner.NSGAII(problem).run();
    }

    private List<DatasetPlaylist> getSimilarPlaylistsFor(ChallengePlaylist playlist) {
        // TODO

        return new ArrayList<>() {{
            add(datasetRepository.findById(4).orElseThrow());
        }};
    }
}
