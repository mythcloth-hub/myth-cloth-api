package com.mesofi.mythclothapi.collectorspurchases;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseSummaryLineItemReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseSummaryLineItemResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/purchases")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CollectorPurchaseController {

  private final CollectorPurchaseService service;

  @PostMapping("/summary-line-items")
  @PreAuthorize("hasAuthority('purchases:add')")
  public ResponseEntity<CollectorPurchaseSummaryLineItemResp> createSummaryLineItem(
      @AuthenticationPrincipal Jwt jwt,
      @Valid @RequestBody CollectorPurchaseSummaryLineItemReq request) {
    log.info(
        "Creating collector purchase summary with line items. CollectorId: {}, Store: {}, OrderNumber: {}, Items: {}",
        jwt.getSubject(),
        request.store(),
        request.orderNumber(),
        request.lineItems().size());

    CollectorPurchaseSummaryLineItemResp response =
        service.createSummaryLineItem(getCollectorId(jwt), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/summary-line-items/{purchaseId}")
  @PreAuthorize("hasAuthority('purchases:update')")
  public ResponseEntity<CollectorPurchaseSummaryLineItemResp> updateSummaryLineItem(
      @AuthenticationPrincipal Jwt jwt,
      @Positive @PathVariable Long purchaseId,
      @Valid @RequestBody CollectorPurchaseSummaryLineItemReq request) {
    log.info(
        "Updating collector purchase summary with line items. CollectorId: {}, PurchaseId: {}, Store: {}, OrderNumber: {}, Items: {}",
        jwt.getSubject(),
        purchaseId,
        request.store(),
        request.orderNumber(),
        request.lineItems().size());

    CollectorPurchaseSummaryLineItemResp response =
        service.updateSummaryLineItem(getCollectorId(jwt), purchaseId, request);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @PreAuthorize("hasAuthority('purchases:read')")
  public List<CollectorPurchaseSummaryLineItemResp> retrieveSummaryLineItems(
      @AuthenticationPrincipal Jwt jwt) {
    return service.retrieveSummaryLineItems(getCollectorId(jwt));
  }

  @GetMapping("/{purchaseId}")
  @PreAuthorize("hasAuthority('purchases:read')")
  public CollectorPurchaseSummaryLineItemResp retrieveSummaryLineItem(
      @AuthenticationPrincipal Jwt jwt, @Positive @PathVariable Long purchaseId) {
    return service.retrieveSummaryLineItem(getCollectorId(jwt), purchaseId);
  }

  private Long getCollectorId(Jwt jwt) {
    return Long.valueOf(jwt.getSubject());
  }
}
