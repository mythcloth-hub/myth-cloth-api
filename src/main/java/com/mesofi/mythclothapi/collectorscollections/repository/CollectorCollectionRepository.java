package com.mesofi.mythclothapi.collectorscollections.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;

/**
 * Repository for {@link CollectorCollection} persistence and collection summary
 * queries.
 *
 * <p>
 * Provides standard CRUD operations through {@link JpaRepository} and custom
 * lookups used by the collector collection service layer.
 * </p>
 */
@Repository
public interface CollectorCollectionRepository extends JpaRepository<CollectorCollection, Long> {
    /**
     * Counts the collections owned by the specified collector.
     *
     * @param collector
     *            collector whose collection count should be computed
     * @return number of collections owned by the collector
     */
    long countByCollector(Collector collector);

    /**
     * Finds a collection by its unique name.
     *
     * @param collector
     *            collector who owns the collection.
     * @param name
     *            collection name
     * @return matching collection when present
     */
    Optional<CollectorCollection> findByCollectorAndName(Collector collector, String name);

}
