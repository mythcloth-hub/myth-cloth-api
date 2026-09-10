package com.mesofi.mythclothapi.collectorscollections.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionItem;
import com.mesofi.mythclothapi.figurines.model.Figurine;

@Repository
public interface CollectorCollectionItemRepository extends JpaRepository<CollectorCollectionItem, Long> {

    /**
     * Finds a collector collection item by its collection and figurine.
     *
     * @param collection
     *            the collector's collection
     * @param figurine
     *            the figurine
     * @return an optional containing the collector collection item if found, or
     *         empty if not found
     */
    Optional<CollectorCollectionItem> findByCollectionAndFigurine(CollectorCollection collection, Figurine figurine);

    /**
     * Finds all owned figurines in a collection, ordered by the date they were
     * added to the collection in descending order.
     *
     * @param collection
     *            the collector's collection
     * @param pageable
     *            the pagination information
     * @return a list of collector collection figurines
     */
    List<CollectorCollectionItem> findByCollectionAndOwnedTrueOrderByAddedAtDesc(CollectorCollection collection,
            Pageable pageable);
}
