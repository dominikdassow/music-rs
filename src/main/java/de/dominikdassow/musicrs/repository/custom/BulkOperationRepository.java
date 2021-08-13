package de.dominikdassow.musicrs.repository.custom;

import de.dominikdassow.musicrs.model.Identifiable;

import java.util.List;

public interface BulkOperationRepository<T extends Identifiable> {

    int insertMany(List<T> documents, Class<T> entityClass);
}
