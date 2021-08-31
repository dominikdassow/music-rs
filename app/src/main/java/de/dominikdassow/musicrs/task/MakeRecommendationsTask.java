package de.dominikdassow.musicrs.task;

import de.dominikdassow.musicrs.recommender.algorithm.AlgorithmConfiguration;
import de.dominikdassow.musicrs.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.uma.jmetal.solution.Solution;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class MakeRecommendationsTask
    extends Task {

    private RecommendationService recommender;

    private Set<Integer> playlists;

    private List<AlgorithmConfiguration<? extends Solution<Integer>>> algorithmConfigurations;

    public MakeRecommendationsTask() {
        super("Make Recommendation");
    }

    public MakeRecommendationsTask forPlaylists(Integer... playlist) {
        this.playlists = Set.of(playlist);

        return this;
    }

    public MakeRecommendationsTask using(List<AlgorithmConfiguration<? extends Solution<Integer>>> configurations) {
        this.algorithmConfigurations = configurations;

        return this;
    }

    @Override
    protected void init() {
        recommender = new RecommendationService();
    }

    @Override
    protected void execute() {
        List<RecommendationService.Result> recommendations = recommender
            .makeRecommendations(playlists, algorithmConfigurations);

        recommendations.forEach(recommendation -> {
            log.info("### RESULT [playlist=" + recommendation.getPlaylist() + "] " +
                "[" + recommendation.getConfiguration().getName() + "]");

            // TODO: Use recommendations somehow
            recommendation.getTracks().forEach(tracks -> System.out.println(
                tracks.stream().limit(3).collect(Collectors.joining(", ")) +
                    " [+ " + (tracks.size() - 3) + " more]"));

            log.info("###");
        });
    }
}
