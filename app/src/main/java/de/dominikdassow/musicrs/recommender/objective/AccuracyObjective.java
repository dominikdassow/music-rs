package de.dominikdassow.musicrs.recommender.objective;

import de.dominikdassow.musicrs.model.SimilarTracksList;

import java.util.List;

public class AccuracyObjective
    implements Objective {

    private final List<SimilarTracksList> similarTracksLists;

    public AccuracyObjective(List<SimilarTracksList> similarTracksLists) {
        this.similarTracksLists = similarTracksLists;
    }

    @Override
    public double evaluate(List<String> tracks) {
        double fitness = 0.0;

        double numberOfTracks = tracks.size();

        for (int i = 1; i <= tracks.size(); i++) {
            fitness += getTrackListsOccurrencesFactor(tracks.get(i - 1)) * (numberOfTracks / i);
        }

        return fitness;
    }

    @Override
    public double evaluate(String track) {
        return getTrackListsOccurrencesFactor(track);
    }

    private double getTrackListsOccurrencesFactor(String track) {
        double factor = 0.0;

        double numberOfTracksLists = similarTracksLists.size();

        for (int j = 1; j <= similarTracksLists.size(); j++) {
            SimilarTracksList tracksList = similarTracksLists.get(j - 1);

            if (tracksList.contains(track)) {
                factor += (numberOfTracksLists / j) * tracksList.getSimilarity();
            }
        }

        return factor;
    }
}
