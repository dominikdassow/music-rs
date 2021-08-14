package de.dominikdassow.musicrs.repository;

import de.dominikdassow.musicrs.model.playlist.DatasetPlaylist;
import de.dominikdassow.musicrs.repository.custom.BulkOperationRepository;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
public interface DatasetRepository
    extends MongoRepository<DatasetPlaylist, Integer>, BulkOperationRepository<DatasetPlaylist> {

    @Query(value = "{}", sort = "{ _id : 1 }", fields = "{ _id : 1 }")
    Stream<DatasetPlaylist.WithId> streamAllWithId();

    @Query(value = "{}", sort = "{ _id : 1 }", fields = "{ _id : 1, features: 1 }")
    Stream<DatasetPlaylist> streamAllWithIdAndFeatures();

    @Aggregation({
        "{ $match: { _id: ?0 } }",
        "{ $project: { total: { $size: { $objectToArray: '$tracks' } } } }",
    })
    Integer countTracksById(Integer id);

    @Aggregation({
        "{ $match: { _id: ?0 } }",
        "{ $project: { tracks: { $map: { input: { $objectToArray: '$tracks' }, as: 't', in: '$$t.v.$id' } } } }",
        "{ $project: { unique: { $size: { $setDifference: [ '$tracks', [] ] } } } }",
    })
    Integer countUniqueTracksById(Integer id);

    @Aggregation({
        "{ $match: { _id: { $in: ?0 } } }",
        "{ $project: { trackIds: { $map: { input: { $objectToArray: '$tracks' }, as: 't', in: '$$t.v.$id' } } } }",
        "{ $group: { _id: 0, trackIds: { $push: '$trackIds' } } }",
        "{ $project: { trackIds: { $reduce: { input: '$trackIds', initialValue: [], in: { $setUnion: [ '$$value', '$$this' ] } } } } }",
        "{ $project: { unique: { $size: { $setDifference: [ '$trackIds', [] ] } } } }",
    })
    Integer countUniqueTracksByIds(List<Integer> ids);
}
