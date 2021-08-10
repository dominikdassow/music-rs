package de.dominikdassow.musicrs.recommender.objective;

import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.recommender.data.TracksFeaturesData;
import es.uam.eps.ir.ranksys.novdiv.distance.CosineFeatureItemDistanceModel;
import es.uam.eps.ir.ranksys.novdiv.distance.ItemDistanceModel;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DiversityObjective
    implements Objective {

    private final TracksFeaturesData data;
    private final ItemDistanceModel<Track> distanceModel;

    private final Map<String, Double> distances = new HashMap<>();

    public DiversityObjective(TracksFeaturesData data) {
        this.data = data;

        distanceModel = new CosineFeatureItemDistanceModel<>(data);
    }

    @Override
    public double evaluate(List<Integer> tracks) {
        double fitness = 0.0;

        for (int i = 0; i < (tracks.size() - 1); i++) {
            final String key = tracks.get(i) + "#" + tracks.get(i + 1);

            distances.putIfAbsent(key,
                distanceModel.dist(data.iidx2item(tracks.get(i)), data.iidx2item(tracks.get(i + 1))));

            fitness += 1.0 - distances.get(key);
        }

        return fitness;
    }
}
