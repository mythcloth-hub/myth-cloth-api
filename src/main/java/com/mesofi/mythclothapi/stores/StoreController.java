package com.mesofi.mythclothapi.stores;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

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

import com.mesofi.mythclothapi.stores.dto.StoreReq;
import com.mesofi.mythclothapi.stores.dto.StoreResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for managing stores.
 * <p>
 * Exposes endpoints to create, retrieve, update, list, and deactivate stores
 * that provide figurine pricing information.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService service;

    /**
     * Creates a new store.
     *
     * @param storeRequest
     *            the store information
     * @return a {@link ResponseEntity} containing the created store and its
     *         location URI
     */
    @PostMapping
    // @PreAuthorize("hasRole('ADMIN') and hasAuthority('stores:write')")
    public ResponseEntity<StoreResp> createStore(@Valid @RequestBody StoreReq storeRequest) {
        StoreResp response = service.createStore(storeRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest() // e.g. /api/stores
                .path("/{id}") // append /{id}
                .buildAndExpand(response.id()).toUri();

        return ResponseEntity.created(location).body(response);
    }

    /**
     * Retrieves a store by its identifier.
     *
     * @param id
     *            the store identifier
     * @return the requested store
     */
    @GetMapping("/{id}")
    public StoreResp retrieveStore(@PathVariable Long id) {
        return service.retrieveStore(id);
    }

    /**
     * Retrieves all active stores.
     *
     * @return the list of active stores ordered by name
     */
    @GetMapping
    public List<StoreResp> retrieveStores() {
        log.info("Retrieving all existing stores ...");

        return service.retrieveStores();
    }

    /**
     * Updates an existing store.
     *
     * @param id
     *            the identifier of the store to update
     * @param storeRequest
     *            the updated store information
     * @return a {@link ResponseEntity} containing the updated store
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN') and hasAuthority('stores:update')")
    public ResponseEntity<StoreResp> updateStore(@PathVariable Long id, @Valid @RequestBody StoreReq storeRequest) {
        StoreResp updated = service.updateStore(id, storeRequest);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deactivates a store.
     * <p>
     * The store remains in the database but is marked as inactive and is no longer
     * returned by active store queries.
     *
     * @param id
     *            the identifier of the store to deactivate
     * @return a {@link ResponseEntity} with HTTP 204 (No Content)
     */
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN') and hasAuthority('stores:delete')")
    public ResponseEntity<Void> deactivateStore(@PathVariable Long id) {
        service.deactivateStore(id);
        return ResponseEntity.noContent().build();
    }
}
