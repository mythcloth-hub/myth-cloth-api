package com.mesofi.mythclothapi.collectorspurchases.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchase;

/**
 * Repository for managing collector purchase persistence operations.
 *
 * <p>Provides CRUD operations for {@link CollectorPurchase} entities and custom queries to retrieve
 * purchases scoped to a specific collector.
 *
 * <p>All queries enforce collector ownership by filtering purchases using the collector identifier.
 */
@Repository
public interface CollectorPurchaseRepository extends JpaRepository<CollectorPurchase, Long> {
  /**
   * Finds a purchase by its identifier and collector owner.
   *
   * <p>This method ensures that a purchase can only be retrieved when it belongs to the specified
   * collector.
   *
   * @param id purchase identifier
   * @param collectorId collector identifier
   * @return the purchase when found and owned by the collector, otherwise {@link Optional#empty()}
   */
  Optional<CollectorPurchase> findByIdAndCollectorId(Long id, Long collectorId);

  /**
   * Retrieves all purchases belonging to a collector ordered by most recent purchase date.
   *
   * <p>When multiple purchases have the same order date, results are ordered by purchase identifier
   * descending to provide a deterministic order.
   *
   * @param collectorId collector identifier
   * @return list of purchases ordered by order date descending
   */
  List<CollectorPurchase> findByCollectorIdOrderByOrderDateDescIdDesc(Long collectorId);
}
