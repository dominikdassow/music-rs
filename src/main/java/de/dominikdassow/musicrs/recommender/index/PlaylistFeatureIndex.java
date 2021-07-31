package de.dominikdassow.musicrs.recommender.index;

import de.dominikdassow.musicrs.model.AnyPlaylist;
import de.dominikdassow.musicrs.model.DatasetPlaylist;
import de.dominikdassow.musicrs.model.PlaylistFeature;
import de.dominikdassow.musicrs.repository.DatasetRepository;
import es.uam.eps.ir.ranksys.fast.index.FastItemIndex;
import es.uam.eps.ir.ranksys.fast.index.FastUserIndex;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@SuppressWarnings("unused")
public class PlaylistFeatureIndex
    implements FastUserIndex<AnyPlaylist>, FastItemIndex<PlaylistFeature> {

    @Autowired
    private DatasetRepository datasetRepository;

    @Getter
    private final Map<Integer, DatasetPlaylist> playlists = new HashMap<>();

    @Getter
    private final Map<Integer, PlaylistFeature> playlistFeatures = new HashMap<>();

    @Getter
    private final Map<Integer, List<PlaylistFeature>> featuresByPlaylist = new HashMap<>();

    @Getter
    private final Map<Integer, List<DatasetPlaylist>> playlistsByFeature = new HashMap<>();

    public void init() {
        playlists.clear();
        playlistFeatures.clear();
        featuresByPlaylist.clear();
        playlistsByFeature.clear();

        // TODO: Use database for storing this data
        datasetRepository.findAll().forEach(playlist -> {
            playlists.put(playlist.getId(), playlist);

            final Map<String, Double> tracks = new HashMap<>();
            final Map<String, Double> artists = new HashMap<>();
            final Map<String, Double> albums = new HashMap<>();

            playlist.getTracks().values().forEach(track -> {
                tracks.put(track.getUri(), artists.getOrDefault(track.getUri(), 0.0) + 0.75);
                artists.put(track.getArtistUri(), artists.getOrDefault(track.getArtistUri(), 0.0) + 1.25);
                albums.put(track.getAlbumUri(), albums.getOrDefault(track.getAlbumUri(), 0.0) + 1.0);
            });

            featuresByPlaylist.put(playlist.getId(), new ArrayList<>());

            tracks.forEach((identifier, value) ->
                new PlaylistFeature(PlaylistFeature.Type.TRACK, identifier, value) {{
                    featuresByPlaylist.get(playlist.getId()).add(this);
                    playlistFeatures.put(getId(), this);
                }});

            artists.forEach((identifier, value) ->
                new PlaylistFeature(PlaylistFeature.Type.ARTIST, identifier, value) {{
                    featuresByPlaylist.get(playlist.getId()).add(this);
                    playlistFeatures.put(getId(), this);
                }});

            albums.forEach((identifier, value) ->
                new PlaylistFeature(PlaylistFeature.Type.ALBUM, identifier, value) {{
                    featuresByPlaylist.get(playlist.getId()).add(this);
                    playlistFeatures.put(getId(), this);
                }});
        });

        featuresByPlaylist.entrySet().parallelStream()
            .forEach(entry -> entry.getValue().forEach(playlistFeature -> {
                List<DatasetPlaylist> playlistsWithFeature
                    = playlistsByFeature.getOrDefault(playlistFeature.getId(), new ArrayList<>());

                playlistsWithFeature.add(playlists.get(entry.getKey()));

                playlistsByFeature.put(playlistFeature.getId(), playlistsWithFeature);
            }));
    }

    @Override
    public int user2uidx(AnyPlaylist playlist) {
        return playlist.getId();
    }

    @Override
    public int item2iidx(PlaylistFeature playlistFeature) {
        return playlistFeature.getId();
    }

    @Override
    public AnyPlaylist uidx2user(int uidx) {
        return playlists.get(uidx);
    }

    @Override
    public PlaylistFeature iidx2item(int iidx) {
        return playlistFeatures.get(iidx);
    }

    @Override
    public int numUsers() {
        return playlists.size();
    }

    @Override
    public int numItems() {
        return playlistFeatures.size();
    }
}
