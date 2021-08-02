package de.dominikdassow.musicrs.repository;

import de.dominikdassow.musicrs.model.DatasetPlaylist;
import de.dominikdassow.musicrs.repository.custom.BulkOperationRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface DatasetRepository
    extends MongoRepository<DatasetPlaylist, Integer>, BulkOperationRepository<DatasetPlaylist> {

    @Query(value = "{}", sort = "{ _id : 1 }", fields = "{ _id : 1, features: 1 }")
    Stream<DatasetPlaylist> streamAllWithIdAndFeatures();
}
