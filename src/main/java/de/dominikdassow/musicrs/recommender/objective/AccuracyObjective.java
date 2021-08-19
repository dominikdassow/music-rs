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

        final double numberOfPlaylists = similarTracksLists.size();
        final double numberOfTracks = tracks.size();

        for (int i = 1; i <= tracks.size(); i++) {
            final String trackId = tracks.get(i - 1);

            for (int j = 1; j <= similarTracksLists.size(); j++) {
                final SimilarTracksList trackList = similarTracksLists.get(j - 1);

                if (trackList.contains(trackId)) {
                    fitness += (numberOfTracks / i) * (numberOfPlaylists / j) * trackList.getSimilarity();
                }
            }
        }

        return fitness;
    }
}
