package de.dominikdassow.musicrs.recommender.objective;

import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.playlist.SimilarPlaylist;

import java.util.List;

public class NoveltyObjective
    implements Objective {

    private final List<SimilarPlaylist> similarPlaylists;

    public NoveltyObjective(List<SimilarPlaylist> similarPlaylists) {
        this.similarPlaylists = similarPlaylists;
    }

    @Override
    public double evaluate(List<Track> tracks) {
        double fitness = 0.0;

        final double numberOfTracks = tracks.size();

        for (int i = 1; i <= tracks.size(); i++) {
            final Track track = tracks.get(i - 1);

            final double numberOfMatchingPlaylists = Double.longBitsToDouble(similarPlaylists.stream()
                .filter(playlist -> playlist.getTracks().containsValue(track)).count());

            if (numberOfMatchingPlaylists > 0) {
                fitness += (1 / numberOfMatchingPlaylists) * (numberOfTracks / i);
            } else {
                fitness += (numberOfTracks / i);
            }
        }

        return fitness;
    }
}
