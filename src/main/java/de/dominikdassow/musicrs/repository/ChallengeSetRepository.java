package de.dominikdassow.musicrs.repository;

import de.dominikdassow.musicrs.model.playlist.ChallengePlaylist;
import de.dominikdassow.musicrs.repository.custom.BulkOperationRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface ChallengeSetRepository
    extends MongoRepository<ChallengePlaylist, Integer>, BulkOperationRepository<ChallengePlaylist> {

    @Query(value = "{}", sort = "{ _id : 1 }", fields = "{ _id : 1, features: 1 }")
    Stream<ChallengePlaylist> streamAllWithIdAndFeatures();
}
