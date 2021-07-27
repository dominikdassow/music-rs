package de.dominikdassow.musicrs.repository;

import de.dominikdassow.musicrs.model.DatasetPlaylist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasetRepository extends MongoRepository<DatasetPlaylist, Integer> {

    @Query(value = "{ 'tracks.uri': ?0 }", count = true)
    long countPlaylistsContainingTrackUri(String trackUri);

    @Query("{ 'tracks.uri': ?0 }")
    List<DatasetPlaylist> findPlaylistsContainingTrackUri(String trackUri);
}
