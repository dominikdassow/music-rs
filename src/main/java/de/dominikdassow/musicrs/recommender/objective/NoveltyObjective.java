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
        return 0; // TODO
    }
}
