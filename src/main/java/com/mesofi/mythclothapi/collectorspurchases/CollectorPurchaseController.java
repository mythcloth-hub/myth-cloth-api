package com.mesofi.mythclothapi.collectorspurchases;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseResp;
import com.mesofi.mythclothapi.security.permissions.model.Permissions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for managing collector purchases.
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/collectors")
public class CollectorPurchaseController {

    private final CollectorPurchaseService collectorPurchaseService;

    /**
     * Creates a new collector purchase for the specified collection.
     *
     * @param jwt
     *            the authenticated collector JWT token
     * @param collectionId
     *            the identifier of the collection to which the purchase belongs
     * @param purchaseRequest
     *            the request body containing the details of the purchase to be
     *            created
     * @return a ResponseEntity containing the created purchase response and the
     *         location of the new resource
     */
    @PostMapping("/collections/{collectionId}")
    @PreAuthorize("hasAuthority('" + Permissions.PURCHASES_CREATE + "')")
    public ResponseEntity<CollectorPurchaseResp> createPurchase(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long collectionId, @RequestBody @Valid CollectorPurchaseReq purchaseRequest) {

        CollectorPurchaseResp response = collectorPurchaseService.createPurchase(getCollectorId(jwt), collectionId,
                purchaseRequest);
        log.info("Created collector purchase with ID {}", response.purchaseId());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}") // append /{id}
                .buildAndExpand(response.purchaseId()).toUri();

        return ResponseEntity.created(location).body(response);
    }

    /**
     * Extracts the authenticated collector identifier from the JWT subject claim.
     *
     * @param jwt
     *            authenticated collector JWT token
     * @return collector identifier
     */
    private Long getCollectorId(Jwt jwt) {
        return Long.parseLong(StringUtils.hasText(jwt.getSubject()) ? jwt.getSubject() : "0");
    }
}
