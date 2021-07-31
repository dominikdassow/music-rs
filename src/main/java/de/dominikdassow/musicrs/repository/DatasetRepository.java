package de.dominikdassow.musicrs.repository;

import de.dominikdassow.musicrs.model.DatasetPlaylist;
import de.dominikdassow.musicrs.repository.custom.BulkOperationRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetRepository
    extends MongoRepository<DatasetPlaylist, Integer>, BulkOperationRepository<DatasetPlaylist> {
}