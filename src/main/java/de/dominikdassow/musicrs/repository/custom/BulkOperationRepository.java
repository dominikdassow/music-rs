package de.dominikdassow.musicrs.repository.custom;

import de.dominikdassow.musicrs.model.Identifiable;

import java.util.Map;
import java.util.stream.Stream;

public interface BulkOperationRepository<T extends Identifiable> {

    int insertMany(Map<Integer, T> documents, Stream<? extends Identifiable> existing, Class<T> entityClass);
}
