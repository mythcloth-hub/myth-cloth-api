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
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseLineItemReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseLineItemResp;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseSummaryLineItemReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseSummaryLineItemResp;
import com.mesofi.mythclothapi.collectorspurchases.exceptions.CollectorPurchaseNotFoundException;
import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchase;
import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchaseFigurine;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;
import com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class CollectorPurchaseService {

  private final CollectorPurchaseRepository collectorPurchaseRepository;
  private final CollectorPurchaseFigurineRepository collectorPurchaseFigurineRepository;
  private final CollectorRepository collectorRepository;
  private final FigurineRepository figurineRepository;

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
      collectorPurchaseFigurineRepository
          .findByPurchase(updatedPurchase)
          .forEach(collectorPurchaseFigurineRepository::delete);

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
