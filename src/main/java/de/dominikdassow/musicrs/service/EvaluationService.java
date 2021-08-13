package de.dominikdassow.musicrs.service;

import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationEvaluator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@SuppressWarnings("unused")
public class EvaluationService {

    public Result evaluate(List<Track> relevantTracks, List<Track> recommendedTracks) {
        return new Result(recommendedTracks,
            // TODO: Constant
            new MusicPlaylistContinuationEvaluator(relevantTracks, recommendedTracks.subList(0, 500)));
    }

    @AllArgsConstructor
    public static class Result {

        @Getter
        private final List<Track> tracks;

        private final MusicPlaylistContinuationEvaluator evaluator;

        public double getRPrecision() {
            return evaluator.getRPrecision();
        }

        public double getNDCG() {
            return evaluator.getNDCG();
        }

        public double getRecommendedSongsClicks() {
            return evaluator.getRecommendedSongsClicks();
        }
    }
}
