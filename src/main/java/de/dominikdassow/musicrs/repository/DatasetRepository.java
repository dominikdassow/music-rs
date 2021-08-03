package de.dominikdassow.musicrs.repository;

import de.dominikdassow.musicrs.model.DatasetPlaylist;
import de.dominikdassow.musicrs.repository.custom.BulkOperationRepository;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface DatasetRepository
    extends MongoRepository<DatasetPlaylist, Integer>, BulkOperationRepository<DatasetPlaylist> {

    @Query(value = "{}", sort = "{ _id : 1 }", fields = "{ _id : 1, features: 1 }")
    Stream<DatasetPlaylist> streamAllWithIdAndFeatures();

    @Aggregation({
        "{ $match: { _id: ?0 } }",
        "{ $project: { total: { $size: { $objectToArray: '$tracks' } } } }",
    })
    Integer countTracksById(Integer id);

    @Aggregation({
        "{ $match: { _id: ?0 } }",
        "{ $project: { unique: { $size: { $setDifference: [ { $map: { input: { $objectToArray: '$tracks' }, as: 't', in: '$$t.v.$id' } }, [] ] } } } }",
    })
    Integer countUniqueTracksById(Integer id);
}
