package de.dominikdassow.musicrs.repository.custom;

import de.dominikdassow.musicrs.model.Identifiable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.BulkOperationException;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("unused")
public class BulkOperationRepositoryImpl<T extends Identifiable>
    implements BulkOperationRepository<T> {

    @Autowired
    MongoTemplate mongoTemplate;

    public List<Integer> findAllIds(Class<T> entityClass) {
        return mongoTemplate.findDistinct("_id", entityClass, Integer.class);
    }

    @Override
    public int insertMany(Map<Integer, T> documents, Class<T> entityClass) {
        List<Integer> existingIds = findAllIds(entityClass);

        List<T> insert = documents.entrySet().parallelStream()
            .filter(entry -> !existingIds.contains(entry.getKey()))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());

        if (insert.isEmpty()) {
            return 0;
        }

        try {
            return mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, entityClass)
                .insert(insert)
                .execute()
                .getInsertedCount();
        } catch (BulkOperationException e) {
            return e.getResult().getInsertedCount();
        }
    }
}
