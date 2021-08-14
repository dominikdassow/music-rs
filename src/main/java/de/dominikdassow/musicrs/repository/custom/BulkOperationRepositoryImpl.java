package de.dominikdassow.musicrs.repository.custom;

import de.dominikdassow.musicrs.model.Identifiable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.BulkOperationException;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@SuppressWarnings("unused")
public class BulkOperationRepositoryImpl<T extends Identifiable>
    implements BulkOperationRepository<T> {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public int insertMany(Map<Integer, T> documents, Stream<? extends Identifiable> existing, Class<T> entityClass) {
        existing.parallel()
            .map(Identifiable::getId)
            .forEach(documents::remove);

        if (documents.isEmpty()) {
            return 0;
        }

        try {
            return mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, entityClass)
                .insert(new ArrayList<>(documents.values()))
                .execute()
                .getInsertedCount();
        } catch (BulkOperationException e) {
            return e.getResult().getInsertedCount();
        }
    }
}
