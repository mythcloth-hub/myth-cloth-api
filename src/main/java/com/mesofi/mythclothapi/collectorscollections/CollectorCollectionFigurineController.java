package com.mesofi.mythclothapi.collectorscollections;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mesofi.mythclothapi.collectorscollections.dto.AssignFigurinesReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionResp;
import com.mesofi.mythclothapi.security.permissions.model.Permissions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller responsible for collector collection management and
 * collection-figurine associations.
 *
 * <p>
 * This controller exposes endpoints to:
 *
 * <ul>
 * <li>Assign one or multiple figurines to one or multiple collector
 * collections.
 * <li>Retrieve, update, delete, and duplicate authenticated collector
 * collections.
 * <li>Retrieve and delete figurines associated with a specific collection.
 * </ul>
 *
 * <p>
 * Figurine assignment operations require the authenticated collector identity,
 * which is obtained from the JWT subject claim provided by Spring Security.
 *
 * <p>
 * Authorization is controlled through Spring Security permissions defined with
 * {@code @PreAuthorize}.
 *
 * <p>
 * The preferred way to associate figurines with collections is through
 * {@link #assignFigurinesToCollections(Jwt, AssignFigurinesReq)}. The previous
 * single figurine assignment endpoint
 * {@link #addFigurineToCollection(Jwt, Long, Long)} is deprecated and should no
 * longer be used.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/collections")
@RequiredArgsConstructor
public class CollectorCollectionFigurineController {

    private final CollectorCollectionFigurineService service;

    @PostMapping("/assign-figurines")
    @PreAuthorize("hasAuthority('" + Permissions.COLLECTIONS_FIGURINES_ADD + "')")
    public ResponseEntity<Void> assignFigurinesToCollections(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AssignFigurinesReq request) {
        service.assignFigurinesToCollections(getCollectorId(jwt), request);
        log.info("Assigned figurines {} to collections {} with mode {}", request.figurineIds(), request.collectionIds(),
                request.collectionMode());

        return ResponseEntity.noContent().build();
    }
    /**
     * Retrieves all collections belonging to the authenticated collector.
     *
     * <p>
     * The collector identity is extracted from the JWT subject claim. Access
     * requires the {@code
     * collections:read} authority.
     *
     * @param jwt
     *            authenticated collector's JWT token containing identity
     *            information
     * @return list of collector collections
     */
    @GetMapping
    @PreAuthorize("hasAuthority('" + Permissions.COLLECTIONS_READ + "')")
    public List<CollectorCollectionResp> retrieveCollections(@AuthenticationPrincipal Jwt jwt) {
        return service.retrieveCollections(getCollectorId(jwt));
    }

    /**
     * Extracts the authenticated collector identifier from the JWT subject claim.
     *
     * @param jwt
     *            authenticated collector JWT token
     * @return collector identifier
     */
    private Long getCollectorId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject() == null ? "0" : jwt.getSubject());
    }
}
