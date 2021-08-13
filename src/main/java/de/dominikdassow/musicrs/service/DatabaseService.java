package de.dominikdassow.musicrs.service;

import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.playlist.ChallengePlaylist;
import de.dominikdassow.musicrs.model.playlist.DatasetPlaylist;
import de.dominikdassow.musicrs.repository.ChallengeSetRepository;
import de.dominikdassow.musicrs.repository.DatasetRepository;
import de.dominikdassow.musicrs.repository.TrackRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public List<Track.WithIdAndUri> findAllTracksWithIdAndUri() {
        return mongoTemplate.find(new Query() {{
            fields().include("_id").include("uri");
            with(Sort.by(Sort.Direction.ASC, "_id"));
        }}, Track.WithIdAndUri.class, mongoTemplate.getCollectionName(Track.class));
    }

    public DatasetPlaylist getDatasetPlaylist(Integer id) {
        return datasetRepository.findById(id).orElseThrow();
    }

    public ChallengePlaylist getChallengePlaylist(Integer id) {
        return challengeSetRepository.findById(id).orElseThrow();
    }

    public int insertDatasetPlaylists(List<DatasetPlaylist> playlists) {
        return datasetRepository.insertMany(playlists, DatasetPlaylist.class);
    }

    public int insertChallengePlaylists(List<ChallengePlaylist> playlists) {
        return challengeSetRepository.insertMany(playlists, ChallengePlaylist.class);
    }

    public int insertTracks(List<Track> tracks) {
        return trackRepository.insertMany(tracks, Track.class);
    }

    public void resetDataset() {
        datasetRepository.deleteAll();
        trackRepository.deleteAll();
    }

    public void resetChallengeSet() {
        challengeSetRepository.deleteAll();
    }
}
