package de.dominikdassow.musicrs.task;

import de.dominikdassow.musicrs.model.Playlist;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.service.DatabaseService;
import de.dominikdassow.musicrs.service.EvaluationService;
import de.dominikdassow.musicrs.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@SuppressWarnings("unused")
public class EvaluateSamplePlaylistsTask
    extends Task {

    private static final int SAMPLE_SIZE = 25;

    @Autowired
    private DatabaseService database;

    @Autowired
    private RecommendationService recommender;

    @Autowired
    private EvaluationService evaluator;

    private Set<RecommendationService.AlgorithmType> algorithms = new HashSet<>();

    private final Map<Playlist, List<Track>> samples = new HashMap<>();

    public EvaluateSamplePlaylistsTask() {
        super("Evaluate Sample Playlists");
    }

    public EvaluateSamplePlaylistsTask using(RecommendationService.AlgorithmType... algorithms) {
        this.algorithms = Set.of(algorithms);

        return this;
    }

    public EvaluateSamplePlaylistsTask sampling(Integer... playlistIds) {
        Set.of(playlistIds).forEach(id -> {
            final Playlist playlist = database.getDatasetPlaylist(id);
            final List<Track> trackList = new ArrayList<>(playlist.getTracks().values());

            playlist.setTracks(new HashMap<>() {{
                playlist.getTracks().entrySet().stream().limit(SAMPLE_SIZE)
                    .forEach(entry -> put(entry.getKey(), entry.getValue()));
            }});

            playlist.generateFeatures();

            samples.put(playlist, trackList);
        });

        return this;
    }

    @Override
    protected void execute() {
        List<RecommendationService.Result> recommendations
            = recommender.makeSampledRecommendations(samples.keySet(), algorithms);

        recommendations.forEach(recommendation -> {
            log.info("### RESULT [playlist=" + recommendation.getPlaylist().getId() + "] " +
                "[" + recommendation.getAlgorithm() + "]");

            recommendation.getTracks().forEach(tracks -> {
                EvaluationService.Result evaluation
                    = evaluator.evaluate(samples.get(recommendation.getPlaylist()), tracks);

                log.info("(1) " + tracks.get(0).getUri() + " [" + tracks.get(0).getId() + "]");
                log.info("(2) " + tracks.get(1).getUri() + " [" + tracks.get(1).getId() + "]");
                log.info("(3) " + tracks.get(2).getUri() + " [" + tracks.get(2).getId() + "]");
                log.info("> R-Precision: " + evaluation.getRPrecision());
                log.info("> NDCG: " + evaluation.getNDCG());
                log.info("> Recommended Songs Clicks: " + evaluation.getRecommendedSongsClicks());
            });

            log.info("###");
        });
    }
}
