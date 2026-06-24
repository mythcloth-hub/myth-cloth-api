package com.mesofi.mythclothapi.collectorscollections;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mesofi.mythclothapi.collectorscollections.dto.AssignFigurinesReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectionAssignmentMode;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller responsible for managing the relationship between collectors, collections, and
 * figurines.
 *
 * <p>This controller exposes operations to:
 *
 * <ul>
 *   <li>Assign one or multiple figurines to one or multiple collector collections.
 *   <li>Retrieve collections associated with the authenticated collector.
 * </ul>
 *
 * <p>Figurine assignment operations require the authenticated collector identity, which is obtained
 * from the JWT subject claim provided by Spring Security.
 *
 * <p>Authorization is controlled through Spring Security permissions defined with
 * {@code @PreAuthorize}.
 *
 * <p>The preferred way to associate figurines with collections is through {@link
 * #assignFigurinesToCollections(Jwt, AssignFigurinesReq)}. The previous single figurine assignment
 * endpoint {@link #addFigurineToCollection(Jwt, Long, Long)} is deprecated and should no longer be
 * used.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/collections")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CollectorCollectionFigurineController {

  private final CollectorCollectionFigurineService service;

  /**
   * Adds a single figurine to a specific collector collection.
   *
   * <p>This endpoint is deprecated. Use {@link #assignFigurinesToCollections(Jwt,
   * AssignFigurinesReq)} instead, which supports assigning one or multiple figurines to one or
   * multiple collections using a unified assignment workflow.
   *
   * <p>The authenticated collector is obtained from the JWT subject claim. The operation requires
   * the {@code collections:figurines:add} authority.
   *
   * @param jwt authenticated collector's JWT token containing identity information
   * @param collectionId unique identifier of the target collector collection
   * @param figurineId unique identifier of the figurine to assign
   * @return an empty response with HTTP {@code 204 No Content} when the assignment succeeds
   * @deprecated Use {@link #assignFigurinesToCollections(Jwt, AssignFigurinesReq)} instead.
   */
  @Deprecated
  @PostMapping("/{collectionId}/figurines/{figurineId}")
  @PreAuthorize("hasAuthority('collections:figurines:add')")
  public ResponseEntity<Void> addFigurineToCollection(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long collectionId,
      @PathVariable Long figurineId) {

    AssignFigurinesReq request =
        new AssignFigurinesReq(
            List.of(figurineId), CollectionAssignmentMode.AUTO, List.of(collectionId), null);
    service.assignFigurinesToCollections(getCollectorId(jwt), request);

    return ResponseEntity.noContent().build();
  }

  /**
   * Assigns one or more figurines to one or more collector collections.
   *
   * <p>This endpoint provides the main workflow for managing figurine collection assignments.
   * Depending on the request configuration, it can:
   *
   * <ul>
   *   <li>Assign figurines to existing collections.
   *   <li>Create collections automatically when required.
   *   <li>Apply predefined or user-provided collection information.
   * </ul>
   *
   * <p>The authenticated collector is obtained from the JWT subject claim.
   *
   * @param jwt authenticated collector's JWT token containing identity information
   * @param request assignment request containing figurines, collections, and assignment options
   * @return an empty response with HTTP {@code 204 No Content} when the assignment succeeds
   */
  @PostMapping("/assign-figurines")
  @PreAuthorize("hasAuthority('collections:figurines:add')")
  public ResponseEntity<Void> assignFigurinesToCollections(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AssignFigurinesReq request) {
    service.assignFigurinesToCollections(getCollectorId(jwt), request);

    return ResponseEntity.noContent().build();
  }

  /**
   * Retrieves all collections belonging to the authenticated collector.
   *
   * <p>The collector identity is extracted from the JWT subject claim. Access requires the {@code
   * collections:read} authority.
   *
   * @param jwt authenticated collector's JWT token containing identity information
   * @return list of collector collections
   */
  @GetMapping
  @PreAuthorize("hasAuthority('collections:read')")
  public List<CollectorCollectionResp> retrieveCollections(@AuthenticationPrincipal Jwt jwt) {
    return service.retrieveCollections(getCollectorId(jwt));
  }

  private Long getCollectorId(Jwt jwt) {
    return Long.valueOf(jwt.getSubject());
  }
}
