package com.mesofi.mythclothapi.collectorspurchases.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchase;
import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchaseFigurine;

/**
 * Repository for managing figurine line items associated with collector purchases.
 *
 * <p>A {@link CollectorPurchaseFigurine} represents an individual figurine entry within a purchase,
 * including details such as quantity, price paid, and purchase type.
 *
 * <p>This repository provides operations to retrieve purchase line items by their parent purchase
 * relationship or by purchase identifiers.
 */
@Repository
public interface CollectorPurchaseFigurineRepository
    extends JpaRepository<CollectorPurchaseFigurine, Long> {
  /**
   * Retrieves all figurine line items belonging to a specific purchase.
   *
   * @param purchase the purchase containing the line items
   * @return list of figurine line items associated with the purchase
   */
  List<CollectorPurchaseFigurine> findByPurchase(CollectorPurchase purchase);

  /**
   * Retrieves all figurine line items belonging to a collection of purchases.
   *
   * <p>This method is useful when loading line items for multiple purchases in a single query.
   *
   * @param purchaseIds list of purchase identifiers
   * @return list of figurine line items matching the provided purchase identifiers
   */
  List<CollectorPurchaseFigurine> findByPurchaseIdIn(List<Long> purchaseIds);

  /**
   * Retrieves all figurine line items for a purchase ordered by identifier ascending.
   *
   * <p>The ordering provides deterministic results when displaying purchase details.
   *
   * @param purchaseId purchase identifier
   * @return ordered list of figurine line items
   */
  List<CollectorPurchaseFigurine> findByPurchaseIdOrderByIdAsc(Long purchaseId);
}
