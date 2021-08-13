package de.dominikdassow.musicrs.repository.custom;

import de.dominikdassow.musicrs.model.Identifiable;

import java.util.List;
import java.util.Map;

public interface BulkOperationRepository<T extends Identifiable> {

    List<Integer> findAllIds(Class<T> entityClass);

    int insertMany(Map<Integer, T> documents, Class<T> entityClass);
}
