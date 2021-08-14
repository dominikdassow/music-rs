package de.dominikdassow.musicrs.service;

import com.google.common.collect.Iterators;
import com.google.common.collect.Streams;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.playlist.ChallengePlaylist;
import de.dominikdassow.musicrs.model.playlist.DatasetPlaylist;
import de.dominikdassow.musicrs.repository.ChallengeSetRepository;
import de.dominikdassow.musicrs.repository.DatasetRepository;
import de.dominikdassow.musicrs.repository.TrackRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
@SuppressWarnings("unused")
public class DatabaseService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private ChallengeSetRepository challengeSetRepository;

    @Autowired
    private TrackRepository trackRepository;

    public Stream<Track.WithIdAndUri> streamAllTracksWithIdAndUri() {
        return trackRepository.streamAllWithIdAndUri();
    }

    public Stream<List<Track.WithIdAndUri>> streamAllTracksWithIdAndUri(int batchSize) {
        return Streams.stream(Iterators.partition(streamAllTracksWithIdAndUri().iterator(), batchSize));
    }

    public List<Track.WithIdAndUri> findAllTracksWithIdAndUri() {
        return streamAllTracksWithIdAndUri().collect(Collectors.toList());
    }

    public DatasetPlaylist getDatasetPlaylist(Integer id) {
        return datasetRepository.findById(id).orElseThrow();
    }

    public ChallengePlaylist getChallengePlaylist(Integer id) {
        return challengeSetRepository.findById(id).orElseThrow();
    }

    public int insertDatasetPlaylists(Map<Integer, DatasetPlaylist> playlists) {
        return datasetRepository
            .insertMany(playlists, datasetRepository.streamAllWithId(), DatasetPlaylist.class);
    }

    public int insertChallengePlaylists(Map<Integer, ChallengePlaylist> playlists) {
        return challengeSetRepository
            .insertMany(playlists, challengeSetRepository.streamAllWithId(), ChallengePlaylist.class);
    }

    public int insertTracks(Map<Integer, Track> tracks) {
        return trackRepository
            .insertMany(tracks, trackRepository.streamAllWithId(), Track.class);
    }

    public void resetDataset() {
        datasetRepository.deleteAll();
        trackRepository.deleteAll();
    }

    public void resetChallengeSet() {
        challengeSetRepository.deleteAll();
    }
}
