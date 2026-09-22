package com.mesofi.mythclothapi.collectorspurchases;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchase;

/**
 * Repository interface for managing CollectorPurchase entities. This interface
 * extends JpaRepository, providing CRUD operations and additional query methods
 * for the CollectorPurchase entity.
 */
@Repository
public interface CollectorPurchaseRepository extends JpaRepository<CollectorPurchase, Long> {

    /**
     * Finds a list of CollectorPurchase entities associated with the specified
     * Collector, ordered by orderDate in ascending order.
     *
     * @param collector
     *            the Collector entity for which to find purchases
     * @param pageable
     *            the Pageable object specifying pagination and sorting
     * @return a list of CollectorPurchase entities associated with the specified
     *         Collector, ordered by orderDate in ascending order
     */
    List<CollectorPurchase> findByCollectorOrderByOrderDateAsc(Collector collector, Pageable pageable);

    /**
     * Finds a CollectorPurchase entity by its ID and associated Collector.
     *
     * @param purchaseId
     *            the ID of the CollectorPurchase entity to find
     * @param collector
     *            the Collector entity associated with the purchase
     * @return an Optional containing the found CollectorPurchase entity, or empty
     *         if not found
     */
    Optional<CollectorPurchase> findByIdAndCollector(Long purchaseId, Collector collector);

    /**
     * Finds all CollectorPurchase entities associated with the specified Collector
     * and CollectorCollection.
     *
     * @param collector
     *            the Collector entity for which to find purchases
     * @param collection
     *            the CollectorCollection entity for which to find purchases
     * @return a list of CollectorPurchase entities associated with the specified
     *         Collector and CollectorCollection
     */
    List<CollectorPurchase> findAllByCollectorAndCollection(Collector collector, CollectorCollection collection);

}
