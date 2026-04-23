package com.mesofi.mythclothapi.figurines;

import java.net.URI;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.figurines.dto.PaginatedResponse;
import com.mesofi.mythclothapi.figurines.model.Figurine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller exposing CRUD operations for {@link Figurine} resources.
 *
 * <p>This controller is responsible for:
 *
 * <ul>
 *   <li>Handling HTTP requests related to figurine creation, retrieval, updates, and deletion
 *   <li>Triggering Jakarta Bean Validation for incoming request payloads
 *   <li>Delegating all business logic to {@link FigurineService}
 *   <li>Building appropriate HTTP responses, including {@code Location} headers where applicable
 * </ul>
 *
 * <p>All request payloads annotated with {@code @Valid} are validated before reaching the service
 * layer.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/figurines")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FigurineController {

  private final FigurineService service;

  /**
   * Creates a new {@link Figurine} resource.
   *
   * <p>This endpoint:
   *
   * <ul>
   *   <li>Validates the incoming request payload
   *   <li>Delegates figurine creation to the service layer
   *   <li>Returns the created resource representation
   *   <li>Includes a {@code Location} header pointing to the newly created resource
   * </ul>
   *
   * @param figurineRequest validated figurine creation request
   * @return {@link ResponseEntity} with status {@code 201 Created}, the created figurine in the
   *     body, and a {@code Location} header referencing the new resource
   */
  @PostMapping
  public ResponseEntity<FigurineResp> createFigurine(
      @RequestBody @Valid FigurineReq figurineRequest) {

    FigurineResp response = service.createFigurine(figurineRequest);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}") // append /{id}
            .buildAndExpand(response.id())
            .toUri();

    return ResponseEntity.created(location).body(response);
  }

  /**
   * Retrieves an existing {@link Figurine} resource by its identifier.
   *
   * <p>This endpoint:
   *
   * <ul>
   *   <li>Identifies the target figurine using the path variable
   *   <li>Delegates the read operation to the service layer
   *   <li>Returns the resource representation
   * </ul>
   *
   * <p>If the figurine does not exist, an exception from the service layer is expected to be
   * translated into an appropriate HTTP error response (e.g., {@code 404 Not Found}).
   *
   * @param id identifier of the figurine to retrieve
   * @return API response DTO representing the requested figurine
   */
  @GetMapping("/{id}")
  public FigurineResp retrieveFigurine(@PathVariable Long id) {
    return service.readFigurine(id);
  }

  /**
   * Retrieves a paginated list of figurines, optionally filtered by name.
   *
   * <p>If the 'name' parameter is provided and at least 3 characters, performs a paginated search
   * by name. Otherwise, returns all figurines paginated.
   *
   * @param name optional name filter (min 3 chars to trigger search)
   * @param page zero-based page index (must be 0 or greater)
   * @param size number of elements per page (must be between 1 and 100)
   * @return a {@link ResponseEntity} containing a {@link PaginatedResponse}
   */
  @GetMapping
  public ResponseEntity<PaginatedResponse> retrieveFigurines(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) Long lineUpId,
      @RequestParam(required = false) Long seriesId,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
    Page<FigurineResp> result;

    String figurineName = name != null && name.trim().length() >= 3 ? name.trim() : "";

    result = service.filterFigurines(figurineName, lineUpId, seriesId, page, size);

    return ResponseEntity.ok(
        new PaginatedResponse(
            result.getContent(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()));
  }

  /**
   * Updates an existing {@link Figurine} resource.
   *
   * <p>This endpoint:
   *
   * <ul>
   *   <li>Validates the incoming request payload
   *   <li>Identifies the target figurine using the path variable
   *   <li>Delegates the update operation to the service layer
   *   <li>Returns the updated resource representation
   * </ul>
   *
   * @param id identifier of the figurine to update
   * @param figurineRequest validated figurine update request
   * @return {@link ResponseEntity} containing the updated figurine with status {@code 200 OK}
   */
  @PutMapping("/{id}")
  public ResponseEntity<FigurineResp> updateFigurine(
      @PathVariable Long id, @RequestBody @Valid FigurineReq figurineRequest) {
    FigurineResp updated = service.updateFigurine(id, figurineRequest);
    return ResponseEntity.ok(updated);
  }

  /**
   * Deletes an existing {@link Figurine} resource.
   *
   * <p>This endpoint:
   *
   * <ul>
   *   <li>Identifies the target figurine using the path variable
   *   <li>Delegates the deletion operation to the service layer
   *   <li>Returns an empty response with {@code 204 No Content} status
   * </ul>
   *
   * <p>If the figurine does not exist, an exception from the service layer is expected to be
   * translated into an appropriate HTTP error response (e.g., {@code 404 Not Found}).
   *
   * @param id identifier of the figurine to delete
   * @return {@link ResponseEntity} with no content
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteFigurine(@PathVariable Long id) {
    service.deleteFigurine(id);
    return ResponseEntity.noContent().build();
  }
}
