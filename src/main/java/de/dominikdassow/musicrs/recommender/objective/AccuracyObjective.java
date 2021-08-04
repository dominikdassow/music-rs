package de.dominikdassow.musicrs.recommender.objective;

import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.playlist.SimilarPlaylist;

import java.util.List;

public class AccuracyObjective
    implements Objective {

    private final List<SimilarPlaylist> similarPlaylists;

    public AccuracyObjective(List<SimilarPlaylist> similarPlaylists) {
        this.similarPlaylists = similarPlaylists;
    }

    @Override
    public double evaluate(List<Track> tracks) {
        double fitness = 0.0;

        final double numberOfPlaylists = similarPlaylists.size();
        final double numberOfTracks = tracks.size();

        for (int i = 1; i <= tracks.size(); i++) {
            final Track track = tracks.get(i - 1);

            for (int j = 1; j <= similarPlaylists.size(); j++) {
                final SimilarPlaylist playlist = similarPlaylists.get(j - 1);

                if (playlist.getTracks().containsValue(track)) {
                    fitness += (numberOfTracks / i) * (numberOfPlaylists / j) * playlist.getSimilarity();
                }
            }
        }

        return fitness;
    }
}
