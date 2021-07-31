package de.dominikdassow.musicrs.repository;

import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.repository.custom.BulkOperationRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackRepository
    extends MongoRepository<Track, Integer>, BulkOperationRepository<Track> {
}
