package com.mesofi.mythclothapi.collectorscollections;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
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
 * REST controller responsible for managing figurines associated with collector collections.
 *
 * <p>This controller exposes operations to add figurines to a specific collector collection. Access
 * to these operations is controlled using Spring Security authorities.
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
   * Adds a figurine to the specified collector collection.
   *
   * <p>The authenticated user is obtained from the JWT token provided by Spring Security. The
   * operation requires the authenticated principal to have the {@code collections:figurines:add}
   * authority.
   *
   * @param jwt authenticated user's JWT token containing identity and security claims
   * @param collectionId unique identifier of the collector collection
   * @param figurineId unique identifier of the figurine to add
   * @return a {@link ResponseEntity} containing the added figurine information
   */
  @Deprecated
  @PostMapping("/{collectionId}/figurines/{figurineId}")
  // @PreAuthorize("hasAuthority('collections:figurines:add')")
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

  @PostMapping("/assign-figurines")
  public ResponseEntity<Void> assignFigurinesToCollections(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AssignFigurinesReq request) {
    service.assignFigurinesToCollections(getCollectorId(jwt), request);

    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public List<CollectorCollectionResp> retrieveCollections(@AuthenticationPrincipal Jwt jwt) {
    return service.retrieveCollections(getCollectorId(jwt));
  }

  private Long getCollectorId(Jwt jwt) {
    return Long.valueOf(jwt.getSubject());
  }
}
