package com.mesofi.mythclothapi.figurines;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
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
}
