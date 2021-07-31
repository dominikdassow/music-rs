package de.dominikdassow.musicrs.repository;

import de.dominikdassow.musicrs.model.ChallengePlaylist;
import de.dominikdassow.musicrs.repository.custom.BulkOperationRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeSetRepository
    extends MongoRepository<ChallengePlaylist, Integer>, BulkOperationRepository<ChallengePlaylist> {
}
