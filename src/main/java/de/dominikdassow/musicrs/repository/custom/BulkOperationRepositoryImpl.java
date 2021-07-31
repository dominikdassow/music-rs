package de.dominikdassow.musicrs.repository.custom;

import de.dominikdassow.musicrs.model.AnyDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.BulkOperationException;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

@SuppressWarnings("unused")
public class BulkOperationRepositoryImpl<T extends AnyDocument>
    implements BulkOperationRepository<T> {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public int insertMany(List<T> documents, Class<T> entityClass) {
        List<Integer> existingIds = mongoTemplate.findDistinct("_id", entityClass, Integer.class);

        documents.removeIf(document -> existingIds.contains(document.getId()));

        if (documents.isEmpty()) {
            return 0;
        }

        try {
            return mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, entityClass)
                .insert(documents)
                .execute()
                .getInsertedCount();
        } catch (BulkOperationException e) {
            return e.getResult().getInsertedCount();
        }
    }
}