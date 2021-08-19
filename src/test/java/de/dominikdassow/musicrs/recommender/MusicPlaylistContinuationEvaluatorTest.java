package de.dominikdassow.musicrs.recommender;

import de.dominikdassow.musicrs.model.Track;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple4;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MusicPlaylistContinuationEvaluatorTest {

    private static Track make(String track, String artist) {
        return Track.builder()
            .id(track)
            .artistId(artist)
            .build();
    }

    private static Track makeOne(Integer track, Integer artist) {
        return make("T:" + track, "A:" + artist);
    }

    private static Track make(Integer i) {
        return make("T:" + i, "A:" + i);
    }

    private static List<Track> make(List<Integer> i) {
        return i.stream().map(MusicPlaylistContinuationEvaluatorTest::make).collect(Collectors.toList());
    }

    private static List<Track> make(Integer start, Integer end) {
        return new ArrayList<>() {{
            IntStream.range(start, end).forEach(i -> add(make(i)));
        }};
    }

    @Test
    void testRPrecision() {
        Map<Tuple2<List<Track>, List<Track>>, Double> test = Map.ofEntries(
            entry(Tuple.tuple(
                List.of(),
                List.of(make(1))
            ), 0.0),

            // Track level
            entry(Tuple.tuple(
                List.of(makeOne(1, 10), makeOne(2, 11), makeOne(3, 12)),
                List.of(makeOne(4, 13), makeOne(5, 14))
            ), 0.0),
            entry(Tuple.tuple(
                List.of(makeOne(1, 10), makeOne(2, 11), makeOne(3, 12)),
                List.of(makeOne(1, 13), makeOne(2, 14))
            ), 2.0 / 3), // T:1;T:2
            entry(Tuple.tuple(
                List.of(makeOne(1, 10), makeOne(2, 11)),
                List.of(makeOne(1, 12), makeOne(2, 13), makeOne(3, 14))
            ), 2.0 / 2), // T:1;T:2

            // Artist level
            entry(Tuple.tuple(
                List.of(makeOne(1, 3), makeOne(2, 3), makeOne(3, 3)),
                List.of(makeOne(4, 3), makeOne(5, 2))
            ), 0.25 * 1 / 3), // A:3
            entry(Tuple.tuple(
                List.of(make(1), make(2), make(3)),
                List.of(makeOne(4, 1), makeOne(5, 1))
            ), 0.25 * 1 / 3), // A:1
            entry(Tuple.tuple(
                List.of(make(1), make(2)),
                List.of(makeOne(3, 1), makeOne(4, 2))
            ), 0.25 * 2 / 2), // A:1;A:2

            // Track + Artist level
            entry(Tuple.tuple(
                List.of(make(1), make(2)),
                List.of(make(3), make(4))
            ), 0.0),
            entry(Tuple.tuple(
                List.of(makeOne(1, 1), makeOne(2, 1), makeOne(3, 2)),
                List.of(makeOne(3, 2), makeOne(4, 2))
            ), (1 + 0.25 * 1) / 3), // T:3 + A:2
            entry(Tuple.tuple(
                List.of(makeOne(1, 1), makeOne(2, 1), makeOne(3, 2)),
                List.of(makeOne(1, 1), makeOne(2, 1), makeOne(3, 2), makeOne(4, 2))
            ), (3 + 0.25 * 2) / 3) // T:1;T:2;T3 + A:1;A:2
        );

        test.forEach((n, expected) -> {
            MusicPlaylistContinuationEvaluator evaluator
                = new MusicPlaylistContinuationEvaluator(n.v1, n.v2);

            assertEquals(expected, evaluator.getRPrecision(), n.toString());
        });
    }

    @Test
    void testNDCG() {
        Map<Tuple2<List<Integer>, List<Integer>>, Double> test = Map.ofEntries(
            entry(Tuple.tuple(
                List.of(),
                List.of(1)
            ), 0.0),
            entry(Tuple.tuple(
                List.of(1, 2, 3),
                List.of(4, 5, 6)
            ), 0.0),
            entry(Tuple.tuple(
                List.of(1, 2, 3),
                List.of(4, 5, 1)
            ), 0.23463936301137828),
            entry(Tuple.tuple(
                List.of(1, 2, 3),
                List.of(4, 2, 1)
            ), 0.5307212739772434),
            entry(Tuple.tuple(
                List.of(1, 3, 5, 7),
                List.of(3, 2, 1)
            ), 0.5855700749881526),
            entry(Tuple.tuple(
                List.of(1, 2, 3),
                List.of(1, 3, 4)
            ), 0.7653606369886218),
            entry(Tuple.tuple(
                List.of(1, 2, 3),
                List.of(3, 2, 1)
            ), 1.0),
            entry(Tuple.tuple(
                List.of(1, 2, 3),
                List.of(1, 2, 3)
            ), 1.0),
            entry(Tuple.tuple(
                List.of(1, 2, 3),
                List.of(3, 2, 1, 4, 5)
            ), 1.0)
        );

        test.forEach((n, expected) -> {
            MusicPlaylistContinuationEvaluator evaluator
                = new MusicPlaylistContinuationEvaluator(make(n.v1), make(n.v2));

            assertEquals(expected, evaluator.getNDCG(), n.toString());
        });
    }

    @Test
    void testRecommendedSongsClicks() {
        Map<Tuple4<Integer, Integer, Integer, Integer>, Double> test = Map.ofEntries(
            // No Match
            entry(Tuple.tuple(501, 1000, 0, 2), 1.0),
            entry(Tuple.tuple(501, 1000, 0, 20), 3.0),
            entry(Tuple.tuple(501, 1000, 0, 142), 15.0),
            entry(Tuple.tuple(501, 1000, 0, 500), 51.0),

            // Partial Match
            entry(Tuple.tuple(5, 500, 0, 500), 0.0),
            entry(Tuple.tuple(11, 500, 0, 500), 1.0),
            entry(Tuple.tuple(99, 500, 0, 500), 9.0),
            entry(Tuple.tuple(100, 500, 0, 500), 10.0),

            // Full Match
            entry(Tuple.tuple(0, 2, 0, 2), 0.0),
            entry(Tuple.tuple(0, 20, 0, 20), 0.0),
            entry(Tuple.tuple(0, 500, 0, 500), 0.0)
        );

        test.forEach((n, expected) -> {
            MusicPlaylistContinuationEvaluator evaluator
                = new MusicPlaylistContinuationEvaluator(make(n.v1, n.v2), make(n.v3, n.v4));

            assertEquals(expected, evaluator.getRecommendedSongsClicks(), n.toString());
        });
    }

    @Test
    void testTracksMatchExactly() {
        IntStream.of(2, 20, 500).forEach(n -> {
            MusicPlaylistContinuationEvaluator evaluator
                = new MusicPlaylistContinuationEvaluator(make(0, n), make(0, n));

            assertEquals(1.0, evaluator.getRPrecision(), n);
            assertEquals(1.0, evaluator.getNDCG(), n);
            assertEquals(0.0, evaluator.getRecommendedSongsClicks(), n);
        });
    }
}
