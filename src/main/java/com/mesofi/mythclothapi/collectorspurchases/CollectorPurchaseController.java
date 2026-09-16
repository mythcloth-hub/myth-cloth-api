package com.mesofi.mythclothapi.collectorspurchases;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mesofi.mythclothapi.security.permissions.model.Permissions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/purchases")
@RequiredArgsConstructor
public class CollectorPurchaseController {

    private final CollectorPurchaseService collectorPurchaseService;

    /**
     * Creates a new collector purchase.
     *
     * @param purchaseRequest
     *            the request body containing the details of the purchase to be
     *            created
     * @return a ResponseEntity containing the created purchase response and the
     *         location of the new resource
     */
    @PostMapping
    @PreAuthorize("hasAuthority('" + Permissions.PURCHASES_CREATE + "')")
    public ResponseEntity<CollectorPurchaseResp> createPurchase(
            @RequestBody @Valid CollectorPurchaseReq purchaseRequest) {
        CollectorPurchaseResp response = collectorPurchaseService.createPurchase(purchaseRequest);
        log.info("Created collector purchase with ID {}", response.purchaseId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}") // append /{id}
                .buildAndExpand(response.purchaseId()).toUri();

        return ResponseEntity.created(location).body(response);
    }
}
