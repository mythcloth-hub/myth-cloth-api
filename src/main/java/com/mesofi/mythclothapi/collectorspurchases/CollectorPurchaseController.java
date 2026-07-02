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
import org.springframework.web.bind.annotation.DeleteMapping;
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

/**
 * REST controller responsible for managing collector purchase records.
 *
 * <p>This controller exposes endpoints to create, update, retrieve, and delete collector purchases
 * together with their associated figurine line items.
 *
 * <p>All operations are executed within the authenticated collector context. The collector
 * identifier is extracted from the authenticated JWT token and is used to ensure users can only
 * access their own purchase data.
 *
 * <p>Available operations:
 *
 * <ul>
 *   <li>Create a new purchase with line items
 *   <li>Update an existing purchase and its line items
 *   <li>Retrieve all purchases for the authenticated collector
 *   <li>Retrieve a specific purchase
 *   <li>Delete a purchase and its associated line items
 * </ul>
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/purchases")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CollectorPurchaseController {

  private final CollectorPurchaseService service;

  /**
   * Creates a new collector purchase with its associated figurine line items.
   *
   * <p>The collector is obtained from the authenticated JWT token. The request contains purchase
   * metadata and the figurines included in the purchase.
   *
   * @param jwt authenticated collector token
   * @param request purchase information and line items to create
   * @return HTTP 201 response containing the created purchase summary
   */
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

  /**
   * Updates an existing collector purchase and its figurine line items.
   *
   * <p>The purchase must belong to the authenticated collector. Existing line items may be replaced
   * when changes are detected.
   *
   * @param jwt authenticated collector token
   * @param purchaseId identifier of the purchase to update
   * @param request updated purchase information and line items
   * @return updated purchase summary
   */
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

  /**
   * Retrieves all purchases belonging to the authenticated collector.
   *
   * <p>The response contains purchase summaries together with their associated figurine line items.
   *
   * @param jwt authenticated collector token
   * @return list of collector purchase summaries
   */
  @GetMapping("/summary-line-items")
  @PreAuthorize("hasAuthority('purchases:read')")
  public List<CollectorPurchaseSummaryLineItemResp> retrieveSummaryLineItems(
      @AuthenticationPrincipal Jwt jwt) {
    return service.retrieveSummaryLineItems(getCollectorId(jwt));
  }

  /**
   * Retrieves a specific collector purchase by identifier.
   *
   * <p>The purchase must belong to the authenticated collector.
   *
   * @param jwt authenticated collector token
   * @param purchaseId identifier of the purchase
   * @return purchase summary including its figurine line items
   */
  @GetMapping("/summary-line-items/{purchaseId}")
  @PreAuthorize("hasAuthority('purchases:read')")
  public CollectorPurchaseSummaryLineItemResp retrieveSummaryLineItem(
      @AuthenticationPrincipal Jwt jwt, @Positive @PathVariable Long purchaseId) {
    return service.retrieveSummaryLineItem(getCollectorId(jwt), purchaseId);
  }

  /**
   * Deletes a collector purchase and all associated figurine line items.
   *
   * <p>The purchase must belong to the authenticated collector.
   *
   * @param jwt authenticated collector token
   * @param purchaseId identifier of the purchase to delete
   * @return HTTP 204 response when deletion is successful
   */
  @DeleteMapping("/summary-line-items/{purchaseId}")
  @PreAuthorize("hasAuthority('purchases:delete')")
  public ResponseEntity<Void> deleteSummaryLineItem(
      @AuthenticationPrincipal Jwt jwt, @PathVariable Long purchaseId) {
    log.info(
        "Deleting collector purchase summary with line items. CollectorId: {}, PurchaseId: {}",
        jwt.getSubject(),
        purchaseId);
    service.deleteSummaryLineItem(getCollectorId(jwt), purchaseId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Synchronizes figurine quantities from a specific purchase into a collector collection.
   *
   * <p>This operation updates the collection quantities based on the figurines recorded in the
   * purchase. The purchase remains unchanged as historical data.
   *
   * @param jwt authenticated collector token
   * @param purchaseId identifier of the purchase used as synchronization source
   * @param collectionId identifier of the target collection
   * @return HTTP {@code 202 Accepted} when synchronization is requested successfully
   */
  @PutMapping("/{purchaseId}/collections/{collectionId}/sync-total")
  @PreAuthorize("hasAuthority('purchases:update')")
  public ResponseEntity<Void> syncPurchaseFigurineTotals(
      @AuthenticationPrincipal Jwt jwt,
      @Positive @PathVariable Long purchaseId,
      @Positive @PathVariable Long collectionId) {
    log.info(
        "Syncing purchase figurine totals. CollectorId: {}, PurchaseId: {}, CollectionId: {}",
        jwt.getSubject(),
        purchaseId,
        collectionId);

    service.syncPurchaseFigurineTotals(getCollectorId(jwt), purchaseId, collectionId);
    return ResponseEntity.accepted().build();
  }

  /**
   * Synchronizes figurine quantities from all purchases associated with a collector collection.
   *
   * <p>This operation recalculates collection quantities based on purchase history records.
   *
   * @param jwt authenticated collector token
   * @param collectionId identifier of the target collection
   * @return HTTP {@code 202 Accepted} when synchronization is requested successfully
   */
  @PutMapping("/collections/{collectionId}/sync-total")
  @PreAuthorize("hasAuthority('purchases:update')")
  public ResponseEntity<Void> syncAllPurchaseFigurineTotals(
      @AuthenticationPrincipal Jwt jwt, @Positive @PathVariable Long collectionId) {
    log.info(
        "Syncing all purchase figurine totals for collection. CollectorId: {}, CollectionId: {}",
        jwt.getSubject(),
        collectionId);

    // TODO: Uncomment the following line when the service method is implemented
    // service.syncPurchaseFigurineTotals(getCollectorId(jwt),  collectionId);
    return ResponseEntity.accepted().build();
  }

  /**
   * Extracts the collector identifier from the authenticated JWT token.
   *
   * @param jwt authenticated user token
   * @return collector identifier
   */
  private Long getCollectorId(Jwt jwt) {
    return Long.valueOf(jwt.getSubject());
  }
}
