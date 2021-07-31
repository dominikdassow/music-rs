package de.dominikdassow.musicrs.repository.custom;

import de.dominikdassow.musicrs.model.AnyDocument;

import java.util.List;

public interface BulkOperationRepository<T extends AnyDocument> {

    int insertMany(List<T> documents, Class<T> entityClass);
}
