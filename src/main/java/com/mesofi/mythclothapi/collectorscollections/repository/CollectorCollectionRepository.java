package com.mesofi.mythclothapi.collectorscollections.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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
     * Finds all collections owned by the specified collector.
     *
     * @param collector
     *            collector whose collections should be returned
     * @return collections owned by the collector
     */
    List<CollectorCollection> findByCollector(Collector collector);

    /**
     * Finds a collection by its unique name.
     *
     * @param name
     *            collection name
     * @return matching collection when present
     */
    Optional<CollectorCollection> findByName(String name);

    /**
     * Deletes a collection by its identifier.
     *
     * @param id
     *            collection identifier
     */
    @Modifying
    @Query("DELETE FROM CollectorCollection cc WHERE cc.id = :id")
    void deleteCollectionById(Long id);

    /**
     * Retrieves summary statistics for a collector collection.
     *
     * <p>
     * The summary includes total preordered and released copies, as well as the
     * number of distinct preordered and released figurines.
     * </p>
     *
     * @param collectionId
     *            identifier of the collection to summarize
     * @return collection summary projection
     */
    @Query(value = """
            SELECT
                COALESCE(SUM(CASE WHEN f.current_release_status = 'ANNOUNCED' THEN ccf.quantity ELSE 0 END), 0) AS preordered_quantity, -- Total number of preordered copies
                COALESCE(SUM(CASE WHEN f.current_release_status = 'RELEASED' THEN ccf.quantity ELSE 0 END), 0) AS released_quantity,    -- Total number of released copies
                COALESCE(SUM(CASE WHEN f.current_release_status = 'ANNOUNCED' THEN 1 ELSE 0 END), 0) AS preordered_figurines,           -- Number of unique preordered figurines
                COALESCE(SUM(CASE WHEN f.current_release_status = 'RELEASED' THEN 1 ELSE 0 END), 0) AS released_figurines               -- Number of unique released figurines
            FROM collector_collection_figurines ccf, figurines f
            WHERE ccf.figurine_id = f.id
              AND ccf.collection_id = :collectionId
            """, nativeQuery = true)
    CollectorCollectionSummaryProjection getCollectorCollectionSummary(Long collectionId);
}
