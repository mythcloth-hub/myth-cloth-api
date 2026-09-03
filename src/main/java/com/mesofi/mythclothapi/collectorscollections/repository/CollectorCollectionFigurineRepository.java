package com.mesofi.mythclothapi.collectorscollections.repository;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.figurines.model.Figurine;

@Repository
public interface CollectorCollectionFigurineRepository extends JpaRepository<CollectorCollectionFigurine, Long> {

    Optional<CollectorCollectionFigurine> findByCollectionAndFigurine(CollectorCollection collection,
            Figurine figurine);

    /**
     * Finds all figurines in a collection, ordered by the date they were added to
     * the collection in descending order.
     *
     * @param collection
     *            the collector's collection
     * @param pageable
     *            the pagination information
     * @return a list of collector collection figurines
     */
    List<CollectorCollectionFigurine> findByCollectionOrderByAddedAtDesc(CollectorCollection collection,
            Pageable pageable);

    /**
     * Deletes all figurines in a collection for a specific collector.
     *
     * @param collectionId
     *            the ID of the collection
     * @param collectorId
     *            the ID of the collector
     * @return the number of figurines deleted
     */
    @Modifying
    @Transactional
    @Query("""
                delete from CollectorCollectionFigurine ccf
                where ccf.collection.id = :collectionId
                  and ccf.collection.collector.id = :collectorId
            """)
    int deleteByCollectionIdAndCollectorId(@Param("collectionId") Long collectionId,
            @Param("collectorId") Long collectorId);
}
