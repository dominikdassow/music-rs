package de.dominikdassow.musicrs.recommender.data;

import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.feature.TrackFeature;
import de.dominikdassow.musicrs.recommender.index.TrackFeatureIndex;
import es.uam.eps.ir.ranksys.fast.feature.AbstractFastFeatureData;
import org.ranksys.core.util.tuples.Tuple2io;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TracksFeaturesData
    extends AbstractFastFeatureData<Track, TrackFeature, Double> {

    private final TrackFeatureIndex index;

    public TracksFeaturesData(TrackFeatureIndex index) {
        super(index, index);

        this.index = index;
    }

    @Override
    public Stream<Tuple2io<Double>> getIidxFeatures(int iidx) {
        if (!index.getFeaturesByTrack().containsKey(iidx)) return Stream.empty();

        return index.getFeaturesByTrack().get(iidx).stream()
            .map(trackFeature -> new Tuple2io<>(trackFeature.getId(), 1.0));
    }

    @Override
    public Stream<Tuple2io<Double>> getFidxItems(int fidx) {
        if (!index.getTracksByFeature().containsKey(fidx)) return Stream.empty();

        return index.getTracksByFeature().get(fidx).stream()
            .map(track -> new Tuple2io<>(track.getId(), 1.0));
    }

    @Override
    public int numItems(int fidx) {
        if (!index.getTracksByFeature().containsKey(fidx)) return 0;

        return index.getTracksByFeature().get(fidx).size();
    }

    @Override
    public int numFeatures(int iidx) {
        if (!index.getFeaturesByTrack().containsKey(iidx)) return 0;

        return index.getFeaturesByTrack().get(iidx).size();
    }

    @Override
    public IntStream getIidxWithFeatures() {
        return index.getTracks().keySet().stream()
            .mapToInt(Integer::intValue);
    }

    @Override
    public IntStream getFidxWithItems() {
        return index.getFeatures().keySet().stream()
            .mapToInt(Integer::intValue);
    }

    @Override
    public int numItemsWithFeatures() {
        return index.numItems();
    }

    @Override
    public int numFeaturesWithItems() {
        return index.numFeatures();
    }
}
