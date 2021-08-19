package de.dominikdassow.musicrs.task;

import de.dominikdassow.musicrs.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class MakeRecommendationsTask
    extends Task {

    private RecommendationService recommender;

    private Set<Integer> playlists;

    private Set<RecommendationService.AlgorithmType> algorithms;

    public MakeRecommendationsTask() {
        super("Make Recommendation");
    }

    public MakeRecommendationsTask forPlaylists(Integer... playlist) {
        this.playlists = Set.of(playlist);

        return this;
    }

    public MakeRecommendationsTask using(RecommendationService.AlgorithmType... algorithms) {
        this.algorithms = Set.of(algorithms);

        return this;
    }

    @Override
    protected void init() {
        recommender = new RecommendationService();
    }

    @Override
    protected void execute() {
        List<RecommendationService.Result> recommendations = recommender
            .makeRecommendations(playlists, algorithms);

        recommendations.forEach(recommendation -> {
            log.info("### RESULT [playlist=" + recommendation.getPlaylist() + "] " +
                "[" + recommendation.getAlgorithm() + "]");

            // TODO: Use recommendations somehow
            recommendation.getTracks().forEach(tracks -> System.out.println(
                tracks.stream().limit(3).collect(Collectors.joining(", ")) +
                    " [+ " + (tracks.size() - 3) + " more]"));

            log.info("###");
        });
    }
}
