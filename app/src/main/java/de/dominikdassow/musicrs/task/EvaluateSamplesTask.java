package de.dominikdassow.musicrs.task;

import de.dominikdassow.musicrs.model.Playlist;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.feature.FeatureGenerator;
import de.dominikdassow.musicrs.model.feature.TrackFeature;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationEvaluator;
import de.dominikdassow.musicrs.recommender.algorithm.AlgorithmConfiguration;
import de.dominikdassow.musicrs.service.DatabaseService;
import de.dominikdassow.musicrs.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class EvaluateSamplesTask
    extends Task {

    private static final int SAMPLE_SIZE = 25;

    private final Map<Integer, Playlist.Sample> samples = new HashMap<>();

    private RecommendationService recommender;

    private List<AlgorithmConfiguration> algorithmConfigurations;

    public EvaluateSamplesTask() {
        super("Evaluate Sample Playlists");
    }

    public EvaluateSamplesTask using(AlgorithmConfiguration... algorithmConfigurations) {
        this.algorithmConfigurations = List.of(algorithmConfigurations);

        return this;
    }

    public EvaluateSamplesTask sampling(Integer... playlists) {
        DatabaseService.readPlaylistsTracks(Set.of(playlists)).forEach((playlist, tracks) -> {
            Map<String, Map<TrackFeature.Audio, Double>> audioFeatures
                = DatabaseService.readTracksAudioFeatures(tracks.values());

            List<Track> originalTracks
                = DatabaseService.readTracks(tracks.values());

            List<Track> sampleTracks = originalTracks
                .subList(0, Math.min(originalTracks.size(), SAMPLE_SIZE))
                .stream()
                .peek(track -> {
                    track.setAudioFeaturesFrom(audioFeatures);
                    track.setFeatures(FeatureGenerator.generateFor(track));
                })
                .collect(Collectors.toList());

            Playlist samplePlaylist = Playlist.builder()
                .id(playlist)
                .tracks(IntStream.range(0, sampleTracks.size()).boxed()
                    .collect(Collectors.toMap(Function.identity(), sampleTracks::get)))
                .build();

            samplePlaylist.setFeatures(FeatureGenerator.generateFor(samplePlaylist));

            samples.put(playlist, new Playlist.Sample(samplePlaylist, originalTracks));
        });

        return this;
    }

    @Override
    protected void init() {
        recommender = new RecommendationService(samples.values());
    }

    @Override
    protected void execute() {
        List<RecommendationService.Result> recommendations = recommender
            .makeRecommendations(samples.keySet(), algorithmConfigurations);

        Map<String, Track> allRecommendedTracks = new HashMap<>() {{
            DatabaseService.readTracks(recommendations.stream()
                .flatMap(recommendation -> recommendation.getTracks().stream())
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList())
            ).forEach(track -> put(track.getId(), track));
        }};

        recommendations.forEach(recommendation -> {
            log.info("### RESULT [playlist=" + recommendation.getPlaylist() + "] " +
                "[" + recommendation.getConfiguration().toString() + "]");

            List<Double> rPrecision = new ArrayList<>();
            List<Double> ndcg = new ArrayList<>();
            List<Double> recommendedSongClicks = new ArrayList<>();

            recommendation.getTracks().forEach(tracks -> {
                final MusicPlaylistContinuationEvaluator evaluator = new MusicPlaylistContinuationEvaluator(
                    samples.get(recommendation.getPlaylist()).getOriginalTracks(),
                    tracks.subList(0, 500).stream() // TODO: Constant
                        .map(allRecommendedTracks::get)
                        .collect(Collectors.toList())
                );

//                log.info("(1) " + tracks.get(0));
//                log.info("(2) " + tracks.get(1));
//                log.info("(3) " + tracks.get(2));
//                log.info("> R-Precision: " + evaluator.getRPrecision());
//                log.info("> NDCG: " + evaluator.getNDCG());
//                log.info("> Recommended Songs Clicks: " + evaluator.getRecommendedSongsClicks());

                rPrecision.add(evaluator.getRPrecision());
                ndcg.add(evaluator.getNDCG());
                recommendedSongClicks.add(evaluator.getRecommendedSongsClicks());
            });

            log.info("###");

            log.info("~ R-Precision: " + rPrecision.stream().mapToDouble(d -> d).average().orElseThrow());
            log.info("~ NDCG: " + ndcg.stream().mapToDouble(d -> d).average().orElseThrow());
            log.info("~ Recommended Songs Clicks: " + recommendedSongClicks.stream().mapToDouble(d -> d).average().orElseThrow());
        });
    }
}
