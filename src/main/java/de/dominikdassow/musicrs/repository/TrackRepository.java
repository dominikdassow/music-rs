package de.dominikdassow.musicrs.repository;

import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.playlist.DatasetPlaylist;
import de.dominikdassow.musicrs.repository.custom.BulkOperationRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface TrackRepository
    extends MongoRepository<Track, Integer>, BulkOperationRepository<Track> {

    @Query(value = "{}", sort = "{ _id : 1 }", fields = "{ _id : 1, uri: 1 }")
    Stream<Track.WithIdAndUri> streamAllWithIdAndUri();
}
