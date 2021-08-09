package de.dominikdassow.musicrs.recommender.objective;

import de.dominikdassow.musicrs.model.playlist.SimilarPlaylist;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

public class NoveltyObjective
    implements Objective {

    private final List<SimilarPlaylist> similarPlaylists;

    public NoveltyObjective(List<SimilarPlaylist> similarPlaylists) {
        this.similarPlaylists = similarPlaylists;
    }

    @Override
    public double evaluate(List<Integer> tracks) {
        double fitness = 0.0;

        final double numberOfTracks = tracks.size();

        for (int i = 1; i <= tracks.size(); i++) {
            final Integer trackId = tracks.get(i - 1);

            final long numberOfMatchingPlaylists = similarPlaylists.stream()
                .filter(playlist -> playlist.containsTrack(trackId)).count();

            if (numberOfMatchingPlaylists > 0) {
                fitness += (1.0 / numberOfMatchingPlaylists) * (numberOfTracks / i);
            } else {
                fitness += (numberOfTracks / i);
            }
        }

        return fitness;
    }
}
