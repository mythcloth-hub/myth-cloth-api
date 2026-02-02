package com.mesofi.mythclothapi.figurines;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.figurines.model.Figurine;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller exposing CRUD operations for {@link Figurine} resources.
 *
 * <p>This controller is responsible for:
 *
 * <ul>
 *   <li>Handling HTTP requests related to figurine creation and updates
 *   <li>Validating incoming request payloads
 *   <li>Delegating business logic to {@link FigurineService}
 *   <li>Building appropriate HTTP responses and location headers
 * </ul>
 *
 * <p>All request payloads are validated using Jakarta Bean Validation before being processed by the
 * service layer.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/figurines")
@RequiredArgsConstructor
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
   * @return {@link ResponseEntity} containing the created figurine and location header
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
   * Retrieves an existing {@link Figurine} resource.
   *
   * <p>This endpoint:
   *
   * <ul>
   *   <li>Identifies the target figurine using the path variable
   *   <li>Delegates the read operation to the service layer
   *   <li>Returns the resource representation
   * </ul>
   *
   * <p>If the figurine does not exist, an exception is propagated from the service layer and
   * translated into the appropriate HTTP error response.
   *
   * @param id identifier of the figurine to retrieve
   * @return API response DTO representing the requested figurine
   */
  @GetMapping("/{id}")
  public FigurineResp retrieveFigurine(@PathVariable Long id) {
    return service.readFigurine(id);
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
   * @return {@link ResponseEntity} containing the updated figurine
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
   * <p>If the figurine does not exist, an exception is propagated from the service layer and
   * translated into the appropriate HTTP error response.
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
