package com.mesofi.mythclothapi.collectorspurchases;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.collectors.Collector;
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
     * Collector, ordered by orderDate in descending order.
     *
     * @param collector
     *            the Collector entity for which to find purchases
     * @param pageable
     *            the Pageable object specifying pagination and sorting
     * @return a list of CollectorPurchase entities associated with the specified
     *         Collector, ordered by orderDate in descending order
     */
    List<CollectorPurchase> findByCollectorOrderByOrderDateDesc(Collector collector, Pageable pageable);

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
}
