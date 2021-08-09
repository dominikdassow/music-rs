package de.dominikdassow.musicrs.recommender;

import de.dominikdassow.musicrs.model.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class MusicPlaylistContinuationEvaluator {

    private final int numberOfRelevantTracks;
    private final int numberOfRecommendedTracks;

    private final List<String> relevantTrackUris = new ArrayList<>();
    private final List<String> relevantArtistUris = new ArrayList<>();
    private final List<String> recommendedTrackUris = new ArrayList<>();
    private final List<String> recommendedArtistUris = new ArrayList<>();

    public MusicPlaylistContinuationEvaluator(List<Track> relevantTracks, List<Track> recommendedTracks) {
        assert recommendedTracks.size() > 0;

        numberOfRelevantTracks = relevantTracks.size();
        numberOfRecommendedTracks = recommendedTracks.size();

        relevantTracks.forEach(track -> {
            relevantTrackUris.add(track.getUri());
            relevantArtistUris.add(track.getArtistUri());
        });

        recommendedTracks.forEach(track -> {
            recommendedTrackUris.add(track.getUri());
            recommendedArtistUris.add(track.getArtistUri());
        });
    }

    public double getRPrecision() {
        if (numberOfRelevantTracks == 0) return 0.0;

        double trackLevel = recommendedTrackUris.stream().reduce(0.0,
            (sum, track) -> sum + (relevantTrackUris.contains(track) ? 1.0 : 0.0), Double::sum);

        double artistLevel = recommendedArtistUris.stream().reduce(0.0,
            (sum, artist) -> sum + (relevantArtistUris.contains(artist) ? 1.0 : 0.0), Double::sum);

        return (trackLevel + 0.25 * artistLevel) / numberOfRelevantTracks;
    }

    public double getNDCG() {
        double dcg = IntStream.range(0, numberOfRecommendedTracks).asDoubleStream()
            .filter(i -> relevantTrackUris.contains(recommendedTrackUris.get((int) i)))
            .reduce(0.0, (r, i) -> r + Math.log(2) / Math.log(i + 2));

        double idcg = IntStream.range(0, numberOfRelevantTracks).asDoubleStream()
            .reduce(0.0, (r, i) -> r + Math.log(2) / Math.log(i + 2));

        if (idcg == 0) return 0.0;

        return dcg / idcg;
    }

    public double getRecommendedSongsClicks() {
        for (int i = 0; i < numberOfRecommendedTracks; i++) {
            if (relevantTrackUris.contains(recommendedTrackUris.get(i))) {
                return Math.floor(i / 10.0);
            }
        }

        return 1 + Math.floor(numberOfRecommendedTracks / 10.0);
    }
}
