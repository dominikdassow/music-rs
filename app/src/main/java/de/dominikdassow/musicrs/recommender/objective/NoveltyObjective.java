package de.dominikdassow.musicrs.recommender.objective;

import de.dominikdassow.musicrs.model.SimilarTracksList;

import java.util.List;

public class NoveltyObjective
    implements Objective {

    private final List<SimilarTracksList> similarTracksLists;

    public NoveltyObjective(List<SimilarTracksList> similarTracksLists) {
        this.similarTracksLists = similarTracksLists;
    }

    @Override
    public double evaluate(List<String> tracks) {
        double fitness = 0.0;

        double numberOfTracks = tracks.size();

        for (int i = 1; i <= tracks.size(); i++) {
            fitness += getMatchingTrackListsFactor(tracks.get(i - 1)) * (numberOfTracks / i);
        }

        return fitness;
    }

    @Override
    public double evaluate(String track) {
        return getMatchingTrackListsFactor(track);
    }

    private double getMatchingTrackListsFactor(String track) {
        long numberOfMatchingTracksLists = similarTracksLists.stream()
            .filter(tracksList -> tracksList.contains(track))
            .count();

        if (numberOfMatchingTracksLists == 0) return 0.0;

        double numberOfTracksLists = similarTracksLists.size();
        double factor = numberOfMatchingTracksLists / numberOfTracksLists;

        return (1.0 / factor);
    }
}
