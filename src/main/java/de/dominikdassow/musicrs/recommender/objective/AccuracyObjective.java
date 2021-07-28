package de.dominikdassow.musicrs.recommender.objective;

import de.dominikdassow.musicrs.model.DatasetPlaylist;
import de.dominikdassow.musicrs.model.Track;

import java.util.List;

public class AccuracyObjective
    implements Objective {

    private final List<DatasetPlaylist> similarPlaylists;

    public AccuracyObjective(List<DatasetPlaylist> similarPlaylists) {
        this.similarPlaylists = similarPlaylists;
    }

    @Override
    public double evaluate(List<Track> tracks) {
        return 0; // TODO
    }
}
