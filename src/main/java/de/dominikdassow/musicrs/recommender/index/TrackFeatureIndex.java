package de.dominikdassow.musicrs.recommender.index;

import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.feature.TrackFeature;
import es.uam.eps.ir.ranksys.fast.index.FastFeatureIndex;
import es.uam.eps.ir.ranksys.fast.index.FastItemIndex;
import lombok.Getter;

import java.util.*;

public class TrackFeatureIndex
    implements FastItemIndex<Track>, FastFeatureIndex<TrackFeature> {

    @Getter
    private final Map<Integer, Track> tracks = new HashMap<>();

    @Getter
    private final Map<Integer, TrackFeature> features = new HashMap<>();

    @Getter
    private final Map<Integer, List<Track>> tracksByFeature = new HashMap<>();

    @Getter
    private final Map<Integer, List<TrackFeature>> featuresByTrack = new HashMap<>();

    public TrackFeatureIndex(Set<Track> tracks) {
        tracks.forEach(track -> {
            this.tracks.put(track.getId(), track);

            List<TrackFeature> features = track.getFeatures();

            features.forEach(feature -> {
                this.features.put(feature.getId(), feature);

                tracksByFeature.putIfAbsent(feature.getId(), new ArrayList<>());
                tracksByFeature.get(feature.getId()).add(track);
            });

            featuresByTrack.put(track.getId(), features);
        });
    }

    @Override
    public int feature2fidx(TrackFeature feature) {
        return feature.getId();
    }

    @Override
    public int item2iidx(Track track) {
        return track.getId();
    }

    @Override
    public TrackFeature fidx2feature(int fidx) {
        return features.get(fidx);
    }

    @Override
    public Track iidx2item(int iidx) {
        return tracks.get(iidx);
    }

    @Override
    public int numFeatures() {
        return features.size();
    }

    @Override
    public int numItems() {
        return tracks.size();
    }
}
