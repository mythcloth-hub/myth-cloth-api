package com.mesofi.mythclothapi.stores;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mesofi.mythclothapi.stores.dto.StoreReq;
import com.mesofi.mythclothapi.stores.dto.StoreResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('stores:write')")
    public ResponseEntity<StoreResp> createStore(@Valid @RequestBody StoreReq storeRequest) {
        StoreResp response = service.createStore(storeRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest() // e.g. /api/stores
                .path("/{id}") // append /{id}
                .buildAndExpand(response.id()).toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public List<StoreResp> retrieveStores() {
        log.info("Retrieving all existing stores ...");

        return service.retrieveStores();
    }

}
