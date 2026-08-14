package com.mesofi.mythclothapi.figurines;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
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
import com.mesofi.mythclothapi.figurines.dto.FigurineSummaryResp;
import com.mesofi.mythclothapi.figurines.dto.PaginatedResp;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.repository.CollectablePageImpl;
import com.mesofi.mythclothapi.security.permissions.model.Permissions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller exposing CRUD and import operations for {@link Figurine}
 * resources.
 *
 * <p>
 * This controller is responsible for:
 *
 * <ul>
 * <li>Triggering bulk imports from the public Google Drive CSV source
 * <li>Handling HTTP requests related to figurine creation, retrieval, updates,
 * and deletion
 * <li>Providing filtered, paginated retrieval with support for name, catalog,
 * characteristic, and release-status filters
 * <li>Exposing lightweight summary and selectable-id projections for UI
 * consumption
 * <li>Retrieving the history of past figurine imports
 * <li>Triggering Jakarta Bean Validation for incoming request payloads
 * <li>Delegating all business logic to {@link FigurineService}
 * <li>Building appropriate HTTP responses, including {@code Location} headers
 * where applicable
 * </ul>
 *
 * <p>
 * All request payloads annotated with {@code @Valid} are validated before
 * reaching the service layer. Write operations and the bulk-load endpoint are
 * protected by Spring Security authorization rules.
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
     * <p>
     * This endpoint:
     *
     * <ul>
     * <li>Validates the incoming request payload
     * <li>Delegates figurine creation to the service layer
     * <li>Returns the created resource representation
     * <li>Includes a {@code Location} header pointing to the newly created resource
     * </ul>
     *
     * @param figurineRequest
     *            validated figurine creation request
     * @return {@link ResponseEntity} with status {@code 201 Created}, the created
     *         figurine in the body, and a {@code Location} header referencing the
     *         new resource
     */
    @PostMapping
    @PreAuthorize("hasAuthority('" + Permissions.FIGURINES_CREATE + "')")
    public ResponseEntity<FigurineResp> createFigurine(@RequestBody @Valid FigurineReq figurineRequest) {

        FigurineResp response = service.createFigurine(figurineRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}") // append /{id}
                .buildAndExpand(response.id()).toUri();

        return ResponseEntity.created(location).body(response);
    }

    /**
     * Retrieves an existing {@link Figurine} resource by its identifier.
     *
     * <p>
     * This endpoint:
     *
     * <ul>
     * <li>Identifies the target figurine using the path variable
     * <li>Delegates the read operation to the service layer
     * <li>Returns the resource representation
     * </ul>
     *
     * <p>
     * If the figurine does not exist, an exception from the service layer is
     * expected to be translated into an appropriate HTTP error response (e.g.,
     * {@code 404 Not Found}).
     *
     * @param id
     *            identifier of the figurine to retrieve
     * @return API response DTO representing the requested figurine
     */
    @GetMapping("/{id}")
    public FigurineResp retrieveFigurine(@PathVariable Long id) {
        return service.readFigurine(id);
    }

    /**
     * Retrieves a paginated list of figurines, optionally filtered by any
     * combination of catalog, characteristic, and release-status criteria.
     *
     * <p>
     * This endpoint:
     *
     * <ul>
     * <li>Resolves the authenticated collector's id (if present) and pre-loads
     * which figurines belong to their collection
     * <li>Builds a {@link FigurineFilter} from all supplied query parameters and
     * delegates to the service layer
     * <li>Returns a paginated response that includes collection-ownership metadata
     * when a collector is authenticated
     * </ul>
     *
     * <p>
     * This endpoint is publicly accessible; however, collection-ownership data in
     * the response is only populated when the request carries a valid JWT.
     *
     * @param authentication
     *            Spring Security authentication context; may be {@code null} for
     *            unauthenticated requests
     * @param collectionId
     *            optional id of a specific collector collection to scope results to
     * @param name
     *            optional name filter (substring match on normalized name)
     * @param lineUpId
     *            optional line-up catalog id filter
     * @param seriesId
     *            optional series catalog id filter
     * @param groupId
     *            optional group catalog id filter
     * @param distributionId
     *            optional distribution catalog id filter
     * @param anniversaryId
     *            optional anniversary id filter
     * @param metalBody
     *            optional filter for metal-body editions
     * @param oce
     *            optional filter for Original Color Edition figurines
     * @param revival
     *            optional filter for revival editions
     * @param plainCloth
     *            optional filter for plain-cloth variants
     * @param broken
     *            optional filter for battle-damaged (broken) variants
     * @param golden
     *            optional filter for golden-armor editions
     * @param gold
     *            optional filter for Gold 24k editions
     * @param manga
     *            optional filter for manga-version figurines
     * @param set
     *            optional filter for multipack sets
     * @param articulable
     *            optional filter for articulable figurines
     * @param releaseStatus
     *            optional release-status filter (e.g. {@code RELEASED},
     *            {@code ANNOUNCED})
     * @param restocks
     *            optional filter to include only restock figurines
     * @param page
     *            zero-based page index; must be {@code 0} or greater
     * @param size
     *            number of elements per page; must be between {@code 1} and
     *            {@code 100}
     * @return {@link ResponseEntity} containing a {@link PaginatedResp} with the
     *         matched figurines and pagination metadata
     */
    @GetMapping
    public ResponseEntity<PaginatedResp> retrieveFigurineDetails(Authentication authentication,
            @RequestParam(required = false) Long collectionId, @RequestParam(required = false) String name,
            @RequestParam(required = false) Long lineUpId, @RequestParam(required = false) Long seriesId,
            @RequestParam(required = false) Long groupId, @RequestParam(required = false) Long distributionId,
            @RequestParam(required = false) Long anniversaryId, @RequestParam(required = false) Boolean metalBody,
            @RequestParam(required = false) Boolean oce, @RequestParam(required = false) Boolean revival,
            @RequestParam(required = false) Boolean plainCloth, @RequestParam(required = false) Boolean broken,
            @RequestParam(required = false) Boolean golden, @RequestParam(required = false) Boolean gold,
            @RequestParam(required = false) Boolean manga, @RequestParam(required = false) Boolean set,
            @RequestParam(required = false) Boolean articulable, @RequestParam(required = false) String releaseStatus,
            @RequestParam(required = false) Boolean restocks, @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        CollectablePageImpl<FigurineResp> result;

        List<Long> figurineIds = new ArrayList<>();
        getCollectorId(authentication).ifPresent(
                collectorId -> figurineIds.addAll(service.retrieveCollectedFigurineIds(collectorId, collectionId)));

        FigurineFilter figurineFilter = FigurineFilterFactory.build(figurineIds, name, lineUpId, seriesId, groupId,
                distributionId, anniversaryId, metalBody, oce, revival, plainCloth, broken, golden, gold, manga, set,
                articulable, releaseStatus, restocks);

        result = service.filterFigurines(figurineFilter, page, size);

        log.info("Total figurines retrieved: {}", result.getContent().size());
        return ResponseEntity.ok(new PaginatedResp(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalCollectables(), result.getTotalPages()));
    }

    private Optional<Long> getCollectorId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                return Optional.of(Long.valueOf(jwtAuth.getToken().getSubject()));
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves a lightweight summary list of all figurines.
     *
     * <p>
     * This endpoint returns a reduced projection of each figurine intended for
     * scenarios where a compact list is needed (e.g. dropdown selection or
     * autocomplete). The summary includes only essential fields such as the display
     * name, line-up, and the first official image URL.
     *
     * <p>
     * This endpoint is publicly accessible.
     *
     * @return list of {@link FigurineSummaryResp} records for all figurines
     */
    @GetMapping("/summary")
    public List<FigurineSummaryResp> retrieveFigurineSummaries() {

        FigurineFilter figurineFilter = FigurineFilterFactory.build(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null);

        return service.retrieveFigurineSummaries(figurineFilter);
    }

    /**
     * Retrieves the ids of figurines that match the supplied filter criteria.
     *
     * <p>
     * This endpoint is intended for UI scenarios where only figurine identifiers
     * are needed (e.g. populating a multi-select component or building a
     * collection). The same filter parameters available on the paginated endpoint
     * are supported, but no pagination is applied and only ids are returned.
     *
     * <p>
     * This endpoint is publicly accessible.
     *
     * @param name
     *            optional name filter (substring match on normalized name)
     * @param lineUpId
     *            optional line-up catalog id filter
     * @param seriesId
     *            optional series catalog id filter
     * @param groupId
     *            optional group catalog id filter
     * @param distributionId
     *            optional distribution catalog id filter
     * @param anniversaryId
     *            optional anniversary id filter
     * @param metalBody
     *            optional filter for metal-body editions
     * @param oce
     *            optional filter for Original Color Edition figurines
     * @param revival
     *            optional filter for revival editions
     * @param plainCloth
     *            optional filter for plain-cloth variants
     * @param broken
     *            optional filter for battle-damaged (broken) variants
     * @param golden
     *            optional filter for golden-armor editions
     * @param gold
     *            optional filter for Gold 24k editions
     * @param manga
     *            optional filter for manga-version figurines
     * @param set
     *            optional filter for multipack sets
     * @param articulable
     *            optional filter for articulable figurines
     * @param releaseStatus
     *            optional release-status filter (e.g. {@code RELEASED},
     *            {@code ANNOUNCED})
     * @param restocks
     *            optional filter to include only restock figurines
     * @return list of figurine ids matching the specified criteria
     */
    @GetMapping("/selectable-ids")
    public List<Long> retrieveSelectableFigurines(@RequestParam(required = false) String name,
            @RequestParam(required = false) Long lineUpId, @RequestParam(required = false) Long seriesId,
            @RequestParam(required = false) Long groupId, @RequestParam(required = false) Long distributionId,
            @RequestParam(required = false) Long anniversaryId, @RequestParam(required = false) Boolean metalBody,
            @RequestParam(required = false) Boolean oce, @RequestParam(required = false) Boolean revival,
            @RequestParam(required = false) Boolean plainCloth, @RequestParam(required = false) Boolean broken,
            @RequestParam(required = false) Boolean golden, @RequestParam(required = false) Boolean gold,
            @RequestParam(required = false) Boolean manga, @RequestParam(required = false) Boolean set,
            @RequestParam(required = false) Boolean articulable, @RequestParam(required = false) String releaseStatus,
            @RequestParam(required = false) Boolean restocks) {

        FigurineFilter figurineFilter = FigurineFilterFactory.build(List.of(), name, lineUpId, seriesId, groupId,
                distributionId, anniversaryId, metalBody, oce, revival, plainCloth, broken, golden, gold, manga, set,
                articulable, releaseStatus, restocks);
        return service.retrieveSelectableFigurines(figurineFilter);
    }

    /**
     * Updates an existing {@link Figurine} resource.
     *
     * <p>
     * This endpoint:
     *
     * <ul>
     * <li>Validates the incoming request payload
     * <li>Identifies the target figurine using the path variable
     * <li>Delegates the update operation to the service layer
     * <li>Returns the updated resource representation
     * </ul>
     *
     * @param id
     *            identifier of the figurine to update
     * @param figurineRequest
     *            validated figurine update request
     * @return {@link ResponseEntity} containing the updated figurine with status
     *         {@code 200 OK}
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN') and hasAuthority('figurines:update')")
    public ResponseEntity<FigurineResp> updateFigurine(@PathVariable Long id,
            @RequestBody @Valid FigurineReq figurineRequest) {
        FigurineResp updated = service.updateFigurine(id, figurineRequest);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes an existing {@link Figurine} resource.
     *
     * <p>
     * This endpoint:
     *
     * <ul>
     * <li>Identifies the target figurine using the path variable
     * <li>Delegates the deletion operation to the service layer
     * <li>Returns an empty response with {@code 204 No Content} status
     * </ul>
     *
     * <p>
     * If the figurine does not exist, an exception from the service layer is
     * expected to be translated into an appropriate HTTP error response (e.g.,
     * {@code 404 Not Found}).
     *
     * @param id
     *            identifier of the figurine to delete
     * @return {@link ResponseEntity} with no content
     */
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN') and hasAuthority('figurines:delete')")
    public ResponseEntity<Void> deleteFigurine(@PathVariable Long id) {
        service.deleteFigurine(id);
        return ResponseEntity.noContent().build();
    }
}
