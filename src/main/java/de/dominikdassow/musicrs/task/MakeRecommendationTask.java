package de.dominikdassow.musicrs.task;

import de.dominikdassow.musicrs.model.Playlist;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.service.DatabaseService;
import de.dominikdassow.musicrs.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@SuppressWarnings("unused")
public class MakeRecommendationTask
    extends Task {

    @Autowired
    private DatabaseService database;

    @Autowired
    private RecommendationService recommender;

    private Set<Playlist> playlists = new HashSet<>();

    private Set<RecommendationService.AlgorithmType> algorithms = new HashSet<>();

    public MakeRecommendationTask() {
        super("Make Recommendation");
    }

    public MakeRecommendationTask forChallengePlaylists(Integer... playlistIds) {
        this.playlists = Arrays.stream(playlistIds)
            .map(database::getChallengePlaylist)
            .collect(Collectors.toSet());

        return this;
    }

    public MakeRecommendationTask using(RecommendationService.AlgorithmType... algorithms) {
        this.algorithms = Set.of(algorithms);

        return this;
    }

    @Override
    protected void execute() {
        List<RecommendationService.Result> recommendations
            = recommender.makeRecommendationsFor(playlists, algorithms);

        recommendations.forEach(recommendation -> {
            log.info("### RESULT [playlist=" + recommendation.getPlaylist().getId() + "] " +
                "[" + recommendation.getAlgorithm() + "]");

            // TODO: Use recommendations somehow
            recommendation.getTracks().forEach(tracks -> System.out.println(
                tracks.stream().limit(3).map(Track::getUri).collect(Collectors.joining(", ")) +
                    " [+ " + (tracks.size() - 3) + " more]"));

            log.info("###");
        });
    }
}
