package de.dominikdassow.musicrs.recommender.index;

import de.dominikdassow.musicrs.model.Playlist;
import de.dominikdassow.musicrs.model.feature.PlaylistFeature;
import de.dominikdassow.musicrs.recommender.feature.playlist.AlbumDimension;
import de.dominikdassow.musicrs.recommender.feature.playlist.ArtistDimension;
import de.dominikdassow.musicrs.recommender.feature.playlist.TrackDimension;
import de.dominikdassow.musicrs.repository.ChallengeSetRepository;
import de.dominikdassow.musicrs.repository.DatasetRepository;
import es.uam.eps.ir.ranksys.fast.index.FastItemIndex;
import es.uam.eps.ir.ranksys.fast.index.FastUserIndex;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
@SuppressWarnings("unused")
public class PlaylistFeatureIndex
    implements FastUserIndex<Integer>, FastItemIndex<Integer> {

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private ChallengeSetRepository challengeSetRepository;

    @Getter
    private final ConcurrentMap<Integer, Double> playlistFeatureValues = new ConcurrentHashMap<>();

    @Getter
    private final ConcurrentMap<Integer, List<Integer>> featuresByPlaylist = new ConcurrentHashMap<>();

    @Getter
    private final ConcurrentMap<Integer, List<Integer>> playlistsByFeature = new ConcurrentHashMap<>();

    private Integer lastPlaylistId = 0;

    public void init(Set<Playlist> excluded) {
        log.info("PlaylistFeatureIndex::init()");

        List<Integer> excludedIds = excluded.stream().map(Playlist::getId).collect(Collectors.toList());
        log.info("> EXCLUDE: " + excludedIds);

        playlistFeatureValues.clear();
        featuresByPlaylist.clear();
        playlistsByFeature.clear();

        StopWatch timer = new StopWatch();
        timer.start();

        Stream.of(
            challengeSetRepository.streamAllWithIdAndFeatures()
                .filter(p -> !excludedIds.contains(p.getId())),
            datasetRepository.streamAllWithIdAndFeatures()
                .filter(p -> !excludedIds.contains(p.getId()))
                .limit(10_000),
            excluded.stream()
        ).flatMap(s -> s).parallel().forEach(playlist -> {
            featuresByPlaylist.put(playlist.getId(), new ArrayList<>());

            playlist.getFeatures().forEach(playlistFeature -> {
                playlistFeatureValues.putIfAbsent(playlistFeature.getId(), playlistFeature.getValue());

                featuresByPlaylist.get(playlist.getId()).add(playlistFeature.getId());

                playlistsByFeature.putIfAbsent(playlistFeature.getId(), new ArrayList<>());
                playlistsByFeature.get(playlistFeature.getId()).add(playlist.getId());
            });
        });

        featuresByPlaylist.keySet().forEach(playlistId -> {
            if (playlistId > lastPlaylistId) lastPlaylistId = playlistId;
        });

        timer.stop();
        log.info("PlaylistFeatureIndex::init() -> " + timer.getTotalTimeSeconds());
    }

    @Override
    public int user2uidx(Integer playlistId) {
        return playlistId;
    }

    @Override
    public int item2iidx(Integer playlistFeatureId) {
        return playlistFeatureId;
    }

    @Override
    public Integer uidx2user(int uidx) {
        return uidx;
    }

    @Override
    public Integer iidx2item(int iidx) {
        return iidx;
    }

    @Override
    public int numUsers() {
        return lastPlaylistId + 1;
    }

    @Override
    public int numItems() {
        return playlistFeatureValues.size();
    }
}
