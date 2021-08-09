package de.dominikdassow.musicrs.recommender.index;

import de.dominikdassow.musicrs.model.AnyDocument;
import de.dominikdassow.musicrs.model.AnyPlaylist;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
@SuppressWarnings("unused")
public class PlaylistFeatureIndex
    implements FastUserIndex<Integer>, FastItemIndex<PlaylistFeature> {

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private ChallengeSetRepository challengeSetRepository;

    @Getter
    private final ConcurrentMap<Integer, PlaylistFeature> playlistFeatures = new ConcurrentHashMap<>();

    @Getter
    private final ConcurrentMap<Integer, List<Integer>> featuresByPlaylist = new ConcurrentHashMap<>();

    @Getter
    private final ConcurrentMap<Integer, List<Integer>> playlistsByFeature = new ConcurrentHashMap<>();

    private Integer lastPlaylistId = 0;

    public void init(List<AnyPlaylist> excluded) {
        log.info("PlaylistFeatureIndex::init()");
        log.info("> EXCLUDE: " + excluded.stream().map(AnyDocument::getId).collect(Collectors.toList()));

        playlistFeatures.clear();
        featuresByPlaylist.clear();
        playlistsByFeature.clear();

        StopWatch timer = new StopWatch();
        timer.start();

        Stream.concat(
            challengeSetRepository.streamAllWithIdAndFeatures(),
            datasetRepository.streamAllWithIdAndFeatures().limit(10_000)
        ).parallel().forEach(playlist -> addToIndex(playlist.getId(), playlist.getFeatures()));

        // TODO: Use playlist.getFeatures() instead of generateFor ?
        excluded.forEach(playlist -> addToIndex(playlist.getId(), generateFor(playlist)));

        timer.stop();
        log.info("PlaylistFeatureIndex::init() -> " + timer.getTotalTimeSeconds());

        featuresByPlaylist.keySet().forEach(playlistId -> {
            if (playlistId > lastPlaylistId) lastPlaylistId = playlistId;
        });
    }

    public static List<PlaylistFeature> generateFor(AnyPlaylist playlist) {
        final List<PlaylistFeature> all = new ArrayList<>();

        new TrackDimension(playlist) {{
            all.addAll(getFeatures());
        }};

        new ArtistDimension(playlist) {{
            all.addAll(getFeatures());
        }};

        new AlbumDimension(playlist) {{
            all.addAll(getFeatures());
        }};

        return all;
    }

    private void addToIndex(Integer id, List<PlaylistFeature> features) {
        featuresByPlaylist.put(id, new ArrayList<>());

        features.forEach(playlistFeature -> {
            playlistFeatures.putIfAbsent(playlistFeature.getId(), playlistFeature);

            featuresByPlaylist.get(id).add(playlistFeature.getId());

            playlistsByFeature.putIfAbsent(playlistFeature.getId(), new ArrayList<>());
            playlistsByFeature.get(playlistFeature.getId()).add(id);
        });
    }

    @Override
    public int user2uidx(Integer playlistId) {
        return playlistId;
    }

    @Override
    public int item2iidx(PlaylistFeature playlistFeature) {
        return playlistFeature.getId();
    }

    @Override
    public Integer uidx2user(int uidx) {
        return uidx;
    }

    @Override
    public PlaylistFeature iidx2item(int iidx) {
        return playlistFeatures.get(iidx);
    }

    @Override
    public int numUsers() {
        return lastPlaylistId + 1;
    }

    @Override
    public int numItems() {
        return playlistFeatures.size();
    }
}
