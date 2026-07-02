package com.mesofi.mythclothapi.collectorspurchases;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.collectors.CollectorRepository;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorNotFoundException;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectionNotFoundException;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionFigurineRepository;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionRepository;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseLineItemReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseLineItemResp;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseSummaryLineItemReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseSummaryLineItemResp;
import com.mesofi.mythclothapi.collectorspurchases.exceptions.CollectorPurchaseNotFoundException;
import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchase;
import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchaseFigurine;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;
import com.mesofi.mythclothapi.collectorspurchases.repository.CollectorPurchaseFigurineRepository;
import com.mesofi.mythclothapi.collectorspurchases.repository.CollectorPurchaseRepository;
import com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for managing collector purchase records and their associated figurine items.
 *
 * <p>This service handles the complete lifecycle of collector purchases, including:
 *
 * <ul>
 *   <li>Creating purchase summaries with their figurine line items
 *   <li>Updating existing purchases and synchronizing line items
 *   <li>Retrieving purchase history for collectors
 *   <li>Deleting purchases and related line items
 *   <li>Calculating purchase totals and figurine quantities
 * </ul>
 *
 * <p>Purchases are tracked separately from the collector's current collection state. This allows
 * preserving purchase history, quantities acquired, prices paid, shipping information, and other
 * transaction details without modifying the current collection inventory.
 *
 * <p>This service validates collector ownership and referenced figurines before performing any
 * persistence operations.
 *
 * @see CollectorPurchase
 * @see CollectorPurchaseFigurine
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class CollectorPurchaseService {

  private final CollectorCollectionFigurineRepository collectorCollectionFigurineRepository;
  private final CollectorCollectionRepository collectorCollectionRepository;
  private final CollectorPurchaseRepository collectorPurchaseRepository;
  private final CollectorPurchaseFigurineRepository collectorPurchaseFigurineRepository;
  private final CollectorRepository collectorRepository;
  private final FigurineRepository figurineRepository;

  /**
   * Creates a new collector purchase summary together with its figurine line items.
   *
   * <p>This operation creates the purchase record, calculates totals, validates referenced
   * figurines, and persists all line items.
   *
   * @param collectorId collector identifier
   * @param request purchase summary data and line items
   * @return the created purchase summary with persisted line items
   * @throws CollectorNotFoundException when the collector does not exist
   * @throws FigurineNotFoundException when a referenced figurine does not exist
   */
  @Transactional
  public CollectorPurchaseSummaryLineItemResp createSummaryLineItem(
      @Positive Long collectorId, @NotNull @Valid CollectorPurchaseSummaryLineItemReq request) {

    var collector =
        collectorRepository
            .findById(collectorId)
            .orElseThrow(() -> new CollectorNotFoundException(collectorId));

    CollectorPurchase purchase = new CollectorPurchase();
    purchase.setCollector(collector);
    mapSummaryFields(request, purchase);
    updatePurchaseCalculatedFields(purchase, request.lineItems(), request.shippingStatus());

    CollectorPurchase savedPurchase = collectorPurchaseRepository.save(purchase);
    List<CollectorPurchaseLineItemResp> lineItems =
        saveLineItems(savedPurchase, request.lineItems());

    log.info(
        "Created collector purchase summary [{}] with [{}] line items for collector [{}]",
        savedPurchase.getId(),
        lineItems.size(),
        collectorId);

    return new CollectorPurchaseSummaryLineItemResp(
        savedPurchase.getId(),
        savedPurchase.getOrderDate(),
        savedPurchase.getStore(),
        savedPurchase.getOrderNumber(),
        savedPurchase.getCurrency(),
        savedPurchase.getTotalAmount(),
        savedPurchase.getTotalFigurines(),
        savedPurchase.getShippingStatus(),
        savedPurchase.getTrackingNumber(),
        savedPurchase.getCarrier(),
        savedPurchase.getShippedDate(),
        savedPurchase.getDeliveredDate(),
        lineItems);
  }

  /**
   * Updates an existing collector purchase summary and its associated figurine line items.
   *
   * <p>If line item information changes, existing items are replaced with the updated values.
   *
   * @param collectorId collector identifier
   * @param purchaseId purchase identifier
   * @param request updated purchase summary data and line items
   * @return the updated purchase summary
   * @throws CollectorNotFoundException when the collector does not exist
   * @throws CollectorPurchaseNotFoundException when the purchase does not exist
   */
  @Transactional
  public CollectorPurchaseSummaryLineItemResp updateSummaryLineItem(
      @Positive Long collectorId,
      @Positive Long purchaseId,
      @NotNull @Valid CollectorPurchaseSummaryLineItemReq request) {

    collectorRepository
        .findById(collectorId)
        .orElseThrow(() -> new CollectorNotFoundException(collectorId));

    CollectorPurchase existing =
        collectorPurchaseRepository
            .findByIdAndCollectorId(purchaseId, collectorId)
            .orElseThrow(() -> new CollectorPurchaseNotFoundException(purchaseId));

    mapSummaryFields(request, existing);
    updatePurchaseCalculatedFields(existing, request.lineItems(), request.shippingStatus());
    CollectorPurchase updatedPurchase = collectorPurchaseRepository.save(existing);

    List<CollectorPurchaseFigurine> existingLineItems =
        collectorPurchaseFigurineRepository.findByPurchase(updatedPurchase);

    List<CollectorPurchaseLineItemResp> lineItems;

    boolean wasThereAnyChange = detectChangesInLineItems(existingLineItems, request.lineItems());
    if (wasThereAnyChange) {
      List<CollectorPurchaseFigurine> toBeDeleted =
          collectorPurchaseFigurineRepository.findByPurchase(updatedPurchase);

      for (CollectorPurchaseFigurine purchaseFigurine : toBeDeleted) {
        collectorPurchaseFigurineRepository.deletePurchaseFigurineById(purchaseFigurine.getId());
        log.info("Deleted collector purchase figurine [{}]", purchaseFigurine.getId());
      }

      lineItems = saveLineItems(updatedPurchase, request.lineItems());
    } else {
      lineItems = existingLineItems.stream().map(this::mapToLineItemResponse).toList();
    }

    log.info(
        "Updated collector purchase summary [{}] with [{}] line items for collector [{}]",
        updatedPurchase.getId(),
        lineItems.size(),
        collectorId);

    return new CollectorPurchaseSummaryLineItemResp(
        updatedPurchase.getId(),
        updatedPurchase.getOrderDate(),
        updatedPurchase.getStore(),
        updatedPurchase.getOrderNumber(),
        updatedPurchase.getCurrency(),
        updatedPurchase.getTotalAmount(),
        updatedPurchase.getTotalFigurines(),
        updatedPurchase.getShippingStatus(),
        updatedPurchase.getTrackingNumber(),
        updatedPurchase.getCarrier(),
        updatedPurchase.getShippedDate(),
        updatedPurchase.getDeliveredDate(),
        lineItems);
  }

  /**
   * Retrieves all purchase summaries for a collector.
   *
   * <p>Results are ordered by purchase date descending and include associated figurine line items.
   *
   * @param collectorId collector identifier
   * @return list of purchase summaries
   * @throws CollectorNotFoundException when the collector does not exist
   */
  @Transactional(readOnly = true)
  public List<CollectorPurchaseSummaryLineItemResp> retrieveSummaryLineItems(
      @Positive Long collectorId) {
    collectorRepository
        .findById(collectorId)
        .orElseThrow(() -> new CollectorNotFoundException(collectorId));

    List<CollectorPurchase> purchases =
        collectorPurchaseRepository.findByCollectorIdOrderByOrderDateDescIdDesc(collectorId);
    if (purchases.isEmpty()) {
      return List.of();
    }

    List<Long> purchaseIds = purchases.stream().map(CollectorPurchase::getId).toList();
    List<CollectorPurchaseFigurine> allLineItems =
        collectorPurchaseFigurineRepository.findByPurchaseIdIn(purchaseIds);

    Map<Long, List<CollectorPurchaseLineItemResp>> lineItemsByPurchaseId = new HashMap<>();
    for (CollectorPurchaseFigurine lineItem : allLineItems) {
      Long purchaseId = lineItem.getPurchase().getId();
      lineItemsByPurchaseId
          .computeIfAbsent(purchaseId, ignored -> new ArrayList<>())
          .add(mapToLineItemResponse(lineItem));
    }

    return purchases.stream()
        .map(
            purchase ->
                mapToSummaryResponse(
                    purchase, lineItemsByPurchaseId.getOrDefault(purchase.getId(), List.of())))
        .toList();
  }

  /**
   * Retrieves a specific purchase summary for a collector.
   *
   * @param collectorId collector identifier
   * @param purchaseId purchase identifier
   * @return purchase summary including its line items
   * @throws CollectorNotFoundException when the collector does not exist
   * @throws CollectorPurchaseNotFoundException when the purchase does not exist
   */
  @Transactional(readOnly = true)
  public CollectorPurchaseSummaryLineItemResp retrieveSummaryLineItem(
      @Positive Long collectorId, @Positive Long purchaseId) {
    collectorRepository
        .findById(collectorId)
        .orElseThrow(() -> new CollectorNotFoundException(collectorId));

    CollectorPurchase purchase =
        collectorPurchaseRepository
            .findByIdAndCollectorId(purchaseId, collectorId)
            .orElseThrow(() -> new CollectorPurchaseNotFoundException(purchaseId));

    List<CollectorPurchaseLineItemResp> lineItems =
        collectorPurchaseFigurineRepository.findByPurchaseIdOrderByIdAsc(purchaseId).stream()
            .map(this::mapToLineItemResponse)
            .toList();

    return mapToSummaryResponse(purchase, lineItems);
  }

  /**
   * Deletes a collector purchase summary and all related figurine line items.
   *
   * @param collectorId collector identifier
   * @param purchaseId purchase identifier
   * @throws CollectorNotFoundException when the collector does not exist
   * @throws CollectorPurchaseNotFoundException when the purchase does not exist
   */
  @Transactional
  public void deleteSummaryLineItem(@Positive Long collectorId, @Positive Long purchaseId) {
    collectorRepository
        .findById(collectorId)
        .orElseThrow(() -> new CollectorNotFoundException(collectorId));

    CollectorPurchase purchase =
        collectorPurchaseRepository
            .findByIdAndCollectorId(purchaseId, collectorId)
            .orElseThrow(() -> new CollectorPurchaseNotFoundException(purchaseId));

    collectorPurchaseFigurineRepository
        .findByPurchase(purchase)
        .forEach(collectorPurchaseFigurineRepository::delete);

    collectorPurchaseRepository.delete(purchase);
  }

  @Transactional
  public void syncPurchaseFigurineTotals(
      Long collectorId, @Positive Long purchaseId, @Positive Long collectionId) {
    collectorRepository
        .findById(collectorId)
        .orElseThrow(() -> new CollectorNotFoundException(collectorId));

    var existingPurchase =
        collectorPurchaseRepository
            .findByIdAndCollectorId(purchaseId, collectorId)
            .orElseThrow(() -> new CollectorPurchaseNotFoundException(purchaseId));

    var existingCollection =
        collectorCollectionRepository
            .findById(collectionId)
            .orElseThrow(() -> new CollectionNotFoundException(collectionId));

    List<CollectorPurchaseFigurine> lineItems =
        collectorPurchaseFigurineRepository.findByPurchase(existingPurchase);

    for (CollectorPurchaseFigurine lineItem : lineItems) {
      // Here you can implement the logic to sync the figurine totals.
      // For example, you might want to update some fields in the lineItem or perform calculations.
      // This is a placeholder for your actual sync logic.
      log.info("Syncing line item [{}] for purchase [{}]", lineItem.getId(), purchaseId);

      collectorCollectionFigurineRepository
          .findByCollectionAndFigurine(existingCollection, lineItem.getFigurine())
          .ifPresent(
              (cf -> {
                // Update the collection figurine with the purchase line item details
                cf.setQuantity(lineItem.getQuantity());
                collectorCollectionFigurineRepository.save(cf);
                log.info(
                    "Updated collection figurine [{}] with new quantity [{}]",
                    cf.getId(),
                    cf.getQuantity());
              }));
    }
  }

  private boolean detectChangesInLineItems(
      List<CollectorPurchaseFigurine> existingLineItems,
      List<CollectorPurchaseLineItemReq> collectorPurchaseLineItemReqs) {

    if (existingLineItems.size() != collectorPurchaseLineItemReqs.size()) {
      return true;
    }

    for (int i = 0; i < existingLineItems.size(); i++) {
      if (!Objects.equals(
              existingLineItems.get(i).getFigurine().getId(),
              collectorPurchaseLineItemReqs.get(i).figurineId())
          || !Objects.equals(
              existingLineItems.get(i).getQuantity(),
              collectorPurchaseLineItemReqs.get(i).quantity())
          || existingLineItems
                  .get(i)
                  .getPricePaid()
                  .compareTo(collectorPurchaseLineItemReqs.get(i).pricePaid())
              != 0
          || existingLineItems.get(i).getPurchaseType()
              != collectorPurchaseLineItemReqs.get(i).purchaseType()) {
        return true;
      }
    }

    return false;
  }

  private void updatePurchaseCalculatedFields(
      CollectorPurchase purchase,
      List<CollectorPurchaseLineItemReq> lineItems,
      ShippingStatus shippingStatus) {
    BigDecimal totalAmount = BigDecimal.ZERO;
    int totalFigurines = 0;
    for (CollectorPurchaseLineItemReq lineItemReq : lineItems) {
      BigDecimal lineTotal =
          lineItemReq.pricePaid().multiply(BigDecimal.valueOf(lineItemReq.quantity()));
      totalAmount = totalAmount.add(lineTotal);
      totalFigurines += lineItemReq.quantity();
    }
    purchase.setTotalAmount(totalAmount);
    purchase.setTotalFigurines(totalFigurines);

    if (shippingStatus == ShippingStatus.SHIPPED) {
      purchase.setShippedDate(LocalDate.now());
    }
    if (shippingStatus == ShippingStatus.DELIVERED) {
      purchase.setDeliveredDate(LocalDate.now());
    }
  }

  private void mapSummaryFields(
      CollectorPurchaseSummaryLineItemReq request, CollectorPurchase purchase) {
    purchase.setOrderDate(request.orderDate());
    purchase.setStore(request.store());
    purchase.setOrderNumber(request.orderNumber());
    purchase.setCurrency(request.currency());
    purchase.setShippingStatus(request.shippingStatus());
    purchase.setTrackingNumber(request.trackingNumber());
    purchase.setCarrier(request.carrier());
  }

  private List<CollectorPurchaseLineItemResp> saveLineItems(
      CollectorPurchase purchase, List<CollectorPurchaseLineItemReq> lineItemRequests) {
    List<CollectorPurchaseLineItemResp> lineItems = new ArrayList<>();

    Set<Long> duplicateFigurineIds = new HashSet<>();
    for (CollectorPurchaseLineItemReq lineItemReq : lineItemRequests) {
      if (duplicateFigurineIds.contains(lineItemReq.figurineId())) {
        throw new IllegalArgumentException(
            "Duplicate figurine ID found in line items: " + lineItemReq.figurineId());
      } else {
        duplicateFigurineIds.add(lineItemReq.figurineId());
      }
    }

    for (CollectorPurchaseLineItemReq lineItemRequest : lineItemRequests) {
      var figurine =
          figurineRepository
              .findById(lineItemRequest.figurineId())
              .orElseThrow(() -> new FigurineNotFoundException(lineItemRequest.figurineId()));

      CollectorPurchaseFigurine lineItem = new CollectorPurchaseFigurine();
      lineItem.setPurchase(purchase);
      lineItem.setFigurine(figurine);
      lineItem.setQuantity(lineItemRequest.quantity());
      lineItem.setPricePaid(lineItemRequest.pricePaid());
      lineItem.setPurchaseType(lineItemRequest.purchaseType());

      CollectorPurchaseFigurine savedLineItem = collectorPurchaseFigurineRepository.save(lineItem);
      lineItems.add(
          new CollectorPurchaseLineItemResp(
              savedLineItem.getId(),
              figurine.getId(),
              savedLineItem.getQuantity(),
              savedLineItem.getPricePaid(),
              savedLineItem.getPurchaseType()));
    }
    return lineItems;
  }

  private CollectorPurchaseSummaryLineItemResp mapToSummaryResponse(
      CollectorPurchase purchase, List<CollectorPurchaseLineItemResp> lineItems) {
    return new CollectorPurchaseSummaryLineItemResp(
        purchase.getId(),
        purchase.getOrderDate(),
        purchase.getStore(),
        purchase.getOrderNumber(),
        purchase.getCurrency(),
        purchase.getTotalAmount(),
        purchase.getTotalFigurines(),
        purchase.getShippingStatus(),
        purchase.getTrackingNumber(),
        purchase.getCarrier(),
        purchase.getShippedDate(),
        purchase.getDeliveredDate(),
        lineItems);
  }

  private CollectorPurchaseLineItemResp mapToLineItemResponse(CollectorPurchaseFigurine lineItem) {
    return new CollectorPurchaseLineItemResp(
        lineItem.getId(),
        lineItem.getFigurine().getId(),
        lineItem.getQuantity(),
        lineItem.getPricePaid(),
        lineItem.getPurchaseType());
  }
}
