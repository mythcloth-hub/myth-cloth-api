package com.mesofi.mythclothapi.collectorscollections;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mesofi.mythclothapi.collectorscollections.dto.AssignFigurinesReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionFigurineResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionLatestFavoriteResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionSummaryResp;
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

    /**
     * Adds a single figurine to the authenticated collector's favorite collection.
     *
     * <p>
     * The favorite collection is determined by the collector's preferences. If no
     * favorite collection exists, a new favorite collection is created. The
     * operation requires the {@code collections:figurines:add} authority.
     *
     * @param jwt
     *            authenticated collector's JWT token containing identity
     *            information
     * @param figurineId
     *            unique identifier of the figurine to assign to the favorite
     *            collection
     * @return an empty response with HTTP {@code 204 No Content} when the
     *         assignment succeeds
     */
    @PostMapping("/favorite/figurines/{figurineId}")
    @PreAuthorize("hasAuthority('" + Permissions.COLLECTIONS_FIGURINES_ADD + "')")
    public ResponseEntity<Void> addFigurineToFavoriteCollection(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long figurineId) {

        service.addFigurineToFavoriteCollection(getCollectorId(jwt), figurineId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Assigns one or more figurines to one or more collector collections.
     *
     * <p>
     * This endpoint provides the main workflow for managing figurine collection
     * assignments. Depending on the request configuration, it can:
     *
     * <ul>
     * <li>Assign figurines to existing collections.
     * <li>Create collections automatically when required.
     * <li>Apply predefined or user-provided collection information.
     * </ul>
     *
     * <p>
     * The authenticated collector is obtained from the JWT subject claim.
     *
     * @param jwt
     *            authenticated collector's JWT token containing identity
     *            information
     * @param request
     *            assignment request containing figurines, collections, and
     *            assignment options
     * @return an empty response with HTTP {@code 204 No Content} when the
     *         assignment succeeds
     */
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
     * Retrieves the summary statistics for a specific collector collection.
     *
     * <p>
     * The collection must belong to the authenticated collector. Access requires
     * the {@code collections:figurines:read} authority.
     * </p>
     *
     * @param jwt
     *            authenticated collector's JWT token containing identity
     *            information
     * @param collectionId
     *            unique identifier of the collector collection
     * @param includeRestocks
     *            optional flag to include restocked figurines in the summary
     * @return collection summary response containing catalog and collection
     *         statistics
     */
    @GetMapping("/{collectionId}/summary")
    @PreAuthorize("hasAuthority('" + Permissions.COLLECTIONS_FIGURINES_READ + "')")
    public CollectorCollectionSummaryResp retrieveCollectionSummary(@AuthenticationPrincipal Jwt jwt,
            @Positive @PathVariable Long collectionId, @RequestParam(required = false) boolean includeRestocks) {
        log.info("Retrieving collection summary for collection {} of collector {}, includeRestocks {}", collectionId,
                getCollectorId(jwt), includeRestocks);

        return service.retrieveCollectionSummary(getCollectorId(jwt), collectionId, includeRestocks);
    }

    /**
     * Retrieves all figurines assigned to a specific collector collection.
     *
     * <p>
     * The collection must belong to the authenticated collector. Access requires
     * the {@code
     * collections:figurines:read} authority.
     *
     * @param jwt
     *            authenticated collector's JWT token containing identity
     *            information
     * @param collectionId
     *            unique identifier of the collector collection
     * @param includeRestocks
     *            optional flag to include restocked figurines in the results
     * @param page
     *            page number for pagination (default is 0)
     * @param size
     *            number of items per page for pagination (default is 50, max is
     *            1000)
     * @return list of figurines assigned to the collection
     */
    @GetMapping("/{collectionId}/figurines")
    @PreAuthorize("hasAuthority('" + Permissions.COLLECTIONS_FIGURINES_READ + "')")
    public Page<CollectorCollectionFigurineResp> retrieveCollectionFigurines(@AuthenticationPrincipal Jwt jwt,
            @Positive @PathVariable Long collectionId, @RequestParam(required = false) boolean includeRestocks,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(1000) int size) {
        log.info("Retrieving figurines for collection {} with pagination: page {}, size {}, includeRestocks {}",
                collectionId, page, size, includeRestocks);

        return service.retrieveCollectionFigurines(getCollectorId(jwt), collectionId, includeRestocks, page, size);
    }
    /**
     * Retrieves the latest figurines from the authenticated collector's favorite
     * collection.
     *
     * <p>
     * The favorite collection is determined by the collector's preferences. Access
     * requires the {@code collections:figurines:read} authority.
     *
     * @param jwt
     *            authenticated collector's JWT token containing identity
     *            information
     * @param limit
     *            maximum number of latest figurines to retrieve (default is 20, max
     *            is 30)
     * @return list of latest figurines from the favorite collection
     */
    @GetMapping("/favorite/figurines/latest")
    @PreAuthorize("hasAuthority('" + Permissions.COLLECTIONS_FIGURINES_READ + "')")
    public List<CollectorCollectionLatestFavoriteResp> retrieveLatestFavoriteCollectionFigurines(
            @AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "20") @Min(1) @Max(30) int limit) {
        Long collectorId = getCollectorId(jwt);

        log.info("Retrieving latest figurines from favorite collection of collector {}, limit {}", collectorId, limit);

        return service.retrieveLatestFavoriteCollectionFigurines(collectorId, limit);
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
