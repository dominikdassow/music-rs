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
    public double evaluate(List<Integer> tracks) {
        double fitness = 0.0;

        final double numberOfTracks = tracks.size();

        for (int i = 1; i <= tracks.size(); i++) {
            final Integer trackId = tracks.get(i - 1);

            final long numberOfMatchingPlaylists = similarTracksLists.stream()
                .filter(playlist -> playlist.contains(trackId)).count();

            if (numberOfMatchingPlaylists > 0) {
                fitness += (1.0 / numberOfMatchingPlaylists) * (numberOfTracks / i);
            } else {
                fitness += (numberOfTracks / i);
            }
        }

        return fitness;
    }
}
