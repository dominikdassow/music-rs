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

        final double numberOfTracks = tracks.size();

        for (int i = 1; i <= tracks.size(); i++) {
            final String trackId = tracks.get(i - 1);

            final long numberOfMatchingTrackLists = similarTracksLists.stream()
                .filter(playlist -> playlist.contains(trackId)).count();

            if (numberOfMatchingTrackLists > 0) {
                fitness += (1.0 / numberOfMatchingTrackLists) * (numberOfTracks / i);
            } else {
                fitness += (numberOfTracks / i);
            }
        }

        return fitness;
    }
}
