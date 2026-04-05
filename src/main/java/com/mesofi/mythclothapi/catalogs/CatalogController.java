package com.mesofi.mythclothapi.catalogs;

import java.net.URI;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for managing catalog resources.
 *
 * <p>Handles HTTP requests for CRUD operations on catalog entries, which are polymorphic
 * descriptive references organized by type (e.g., Groups, Series, LineUps, Distributions). All
 * catalog operations are routed through the {@link CatalogService}, which uses a strategy pattern
 * to handle different catalog types dynamically.
 *
 * <p>The controller uses path variables to specify the catalog type (e.g., {@code groups}, {@code
 * series}) and delegates to the service layer which performs type-specific operations.
 *
 * @see CatalogService
 * @see CatalogType
 * @see CatalogReq
 * @see CatalogResp
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/catalogs")
@RequiredArgsConstructor
public class CatalogController {

  private final CatalogService service;

  /**
   * Creates a new catalog entry of the specified type.
   *
   * <p>This method accepts a catalog type (e.g., {@code groups}, {@code series}, {@code lineups},
   * {@code distributions}) as a path variable and creates a new descriptive entry for that catalog.
   * The response includes a {@code Location} header pointing to the newly created resource.
   *
   * @param catalogType the type of catalog to create the entry in (e.g., {@code groups})
   * @param request the catalog request containing the description of the new entry
   * @return a {@link ResponseEntity} with HTTP 201 (Created) status, the {@code Location} header,
   *     and the created catalog entry as the response body
   * @throws jakarta.validation.ConstraintViolationException if the catalog type or request data is
   *     invalid
   */
  @PostMapping("/{catalogType}")
  public ResponseEntity<CatalogResp> createCatalog(
      @NotNull @Valid @PathVariable CatalogType catalogType,
      @NotNull @Valid @RequestBody CatalogReq request) {

    CatalogResp response = service.createCatalog(catalogType.name(), request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest() // e.g. /api/series
            .path("/{id}") // append /{id}
            .buildAndExpand(response.id())
            .toUri();

    return ResponseEntity.created(location).body(response);
  }

  /**
   * Retrieves a catalog entry by its type and ID.
   *
   * <p>Fetches a specific catalog entry from the repository corresponding to the specified catalog
   * type.
   *
   * @param catalogType the type of catalog (e.g., {@code groups}, {@code series})
   * @param id the unique identifier of the catalog entry to retrieve
   * @return the retrieved catalog entry as a {@link CatalogResp}
   * @throws com.mesofi.mythclothapi.catalogs.exceptions.CatalogNotFoundException if the entry with
   *     the given ID does not exist in the specified catalog type
   * @throws com.mesofi.mythclothapi.catalogs.exceptions.RepositoryNotFoundException if the catalog
   *     type is not recognized
   */
  @GetMapping("/{catalogType}/{id}")
  public CatalogResp retrieveCatalog(@PathVariable CatalogType catalogType, @PathVariable Long id) {
    return service.retrieveCatalog(catalogType.name(), id);
  }

  /**
   * Updates an existing catalog entry with new information.
   *
   * <p>Modifies the description of an existing catalog entry in the specified catalog type. Only
   * the {@code description} field is updated; other fields remain unchanged.
   *
   * @param catalogType the type of catalog containing the entry to update
   * @param id the unique identifier of the catalog entry to update
   * @param catalogReq the request containing the new description
   * @return a {@link ResponseEntity} with HTTP 200 (OK) status and the updated catalog entry
   * @throws com.mesofi.mythclothapi.catalogs.exceptions.CatalogNotFoundException if the entry with
   *     the given ID does not exist in the specified catalog type
   * @throws com.mesofi.mythclothapi.catalogs.exceptions.RepositoryNotFoundException if the catalog
   *     type is not recognized
   * @throws jakarta.validation.ConstraintViolationException if the request data is invalid
   */
  @PutMapping("/{catalogType}/{id}")
  public ResponseEntity<CatalogResp> updateCatalog(
      @NotNull @Valid @PathVariable CatalogType catalogType,
      @PathVariable Long id,
      @Valid @RequestBody CatalogReq catalogReq) {
    CatalogResp updated = service.updateCatalog(catalogType.name(), id, catalogReq);
    return ResponseEntity.ok(updated);
  }

  /**
   * Removes a catalog entry from the specified catalog type.
   *
   * <p>Deletes the catalog entry identified by the given ID from the repository corresponding to
   * the catalog type.
   *
   * @param catalogType the type of catalog containing the entry to delete
   * @param id the unique identifier of the catalog entry to delete
   * @return a {@link ResponseEntity} with HTTP 204 (No Content) status
   * @throws com.mesofi.mythclothapi.catalogs.exceptions.CatalogNotFoundException if the entry with
   *     the given ID does not exist in the specified catalog type
   * @throws com.mesofi.mythclothapi.catalogs.exceptions.RepositoryNotFoundException if the catalog
   *     type is not recognized
   * @throws jakarta.validation.ConstraintViolationException if the catalog type is invalid
   */
  @DeleteMapping("/{catalogType}/{id}")
  public ResponseEntity<?> removeCatalog(
      @NotNull @Valid @PathVariable CatalogType catalogType, @PathVariable Long id) {
    service.deleteCatalog(catalogType.name(), id);
    return ResponseEntity.noContent().build();
  }
}
