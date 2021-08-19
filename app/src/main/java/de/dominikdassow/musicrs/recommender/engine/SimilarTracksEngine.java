package de.dominikdassow.musicrs.recommender.engine;

import de.dominikdassow.musicrs.service.DatabaseService;
import es.uam.eps.ir.ranksys.fast.feature.FastFeatureData;
import es.uam.eps.ir.ranksys.fast.feature.SimpleFastFeatureData;
import es.uam.eps.ir.ranksys.fast.index.FastFeatureIndex;
import es.uam.eps.ir.ranksys.fast.index.FastItemIndex;
import es.uam.eps.ir.ranksys.fast.index.SimpleFastFeatureIndex;
import es.uam.eps.ir.ranksys.fast.index.SimpleFastItemIndex;
import es.uam.eps.ir.ranksys.novdiv.distance.CosineFeatureItemDistanceModel;
import es.uam.eps.ir.ranksys.novdiv.distance.ItemDistanceModel;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public class SimilarTracksEngine {

    private final ItemDistanceModel<String> distanceModel;

    private final Map<String, Double> distances = new HashMap<>();

    public SimilarTracksEngine() {
        log.info("SimilarTracksEngine()");

        Stream<String> tracks = DatabaseService
            .readStore(DatabaseService.Store.TRACK)
            .map(data -> data.split(DatabaseService.DELIMITER))
            .map(data -> data[0]);

        Stream<String> trackFeatures = DatabaseService
            .readStore(DatabaseService.Store.TRACK_FEATURE)
            .map(data -> data.split(DatabaseService.DELIMITER))
            .map(data -> data[1]);

        Stream<Tuple3<String, String, Double>> trackFeatureValues = DatabaseService
            .readStore(DatabaseService.Store.TRACK_FEATURE)
            .map(data -> data.split(DatabaseService.DELIMITER))
            .map(data -> Tuple.tuple(data[0], data[1], Double.parseDouble(data[2])));

        final FastItemIndex<String> trackIndex
            = SimpleFastItemIndex.load(tracks.distinct());

        log.info("TrackIndex :: " + trackIndex.numItems());

        final FastFeatureIndex<String> trackFeatureIndex
            = SimpleFastFeatureIndex.load(trackFeatures.distinct());

        log.info("TrackFeatureIndex :: " + trackFeatureIndex.numFeatures());

        final FastFeatureData<String, String, Double> data
            = SimpleFastFeatureData.load(trackFeatureValues, trackIndex, trackFeatureIndex);

        log.info("Data :: " + data.numFeatures());

        distanceModel = new CosineFeatureItemDistanceModel<>(data);
    }

    public Double getDistanceBetween(String trackId1, String trackId2) {
        final String key = trackId1 + "#" + trackId2;

        if (distances.containsKey(key)) return distances.get(key);

        final double distance = distanceModel.dist(trackId1, trackId2);

        // Cache track ids in both "directions" since the distance is symmetric
        distances.put(trackId1 + "#" + trackId2, distance);
        distances.put(trackId2 + "#" + trackId1, distance);

        return distance;
    }
}
