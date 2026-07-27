
package com.mesofi.mythclothapi.figurinestores;

import java.util.List;

import jakarta.validation.constraints.Positive;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreMatchedResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreMatchedSummaryResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStorePricingResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreUnmatchedResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for managing figurine store listings and their association
 * with canonical figurines.
 * <p>
 * Provides endpoints for reviewing matched and unmatched store listings,
 * manually assigning unmatched listings to canonical figurines, and retrieving
 * real-time pricing information across stores.
 * </p>
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/figurine-stores")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class FigurineStoreController {

    private final FigurineStoreService figurineStoreService;

    /**
     * Retrieves a summary of matched figurine listings grouped by store.
     *
     * @return summaries containing store information and the number of matched
     *         figurine listings
     */
    @GetMapping("/matched-listings/summary")
    @PreAuthorize("hasAuthority('figurines:stores:read')")
    public List<FigurineStoreMatchedSummaryResp> retrieveMatchedFigurineListingSummary() {
        log.info("Retrieving matched figurine listing summary ...");

        return figurineStoreService.retrieveMatchedFigurineListingSummary();
    }

    /**
     * Retrieves matched figurine listings for a specific store.
     *
     * @param storeId
     *            store identifier
     * @return matched figurine listings associated with the specified store
     */
    @GetMapping("/matched-listings/stores/{storeId}")
    @PreAuthorize("hasAuthority('figurines:stores:read')")
    public List<FigurineStoreMatchedResp> retrieveMatchedFigurineListing(@Positive @PathVariable Long storeId) {
        log.info("Retrieving matched figurine listing for store {}", storeId);

        return figurineStoreService.retrieveMatchedFigurineListing(storeId);
    }

    @PostMapping("/matched-listings/figurine-store/{figurineStoreId}")
    @PreAuthorize("hasAuthority('figurines:stores:assign')")
    public void manuallyUnmatchFigurineListing(@Positive @PathVariable Long figurineStoreId) {
        log.info("Manually unmatching figurine store {}", figurineStoreId);

        figurineStoreService.manuallyUnmatchFigurineListing(figurineStoreId);
    }

    /**
     * Retrieves store listings that have not yet been matched to canonical
     * figurines and therefore require manual matching.
     *
     * @return unmatched store listings awaiting manual matching
     */
    @GetMapping("/unmatched-listings")
    @PreAuthorize("hasAuthority('figurines:stores:read')")
    public List<FigurineStoreUnmatchedResp> retrieveUnmatchedFigurineListings() {
        log.info("Retrieving unmatched figurines ...");

        return figurineStoreService.retrieveUnmatchedFigurineListings();
    }

    /**
     * Matches an unmatched store listing to a canonical figurine.
     * <p>
     * Once the listing is matched, it is removed from the unmatched listing queue.
     * </p>
     *
     * @param unmatchedListingId
     *            identifier of the unmatched store listing
     * @param figurineId
     *            identifier of the canonical figurine
     */
    @PostMapping("/unmatched-listings/{unmatchedListingId}/figurines/{figurineId}/match")
    @PreAuthorize("hasAuthority('figurines:stores:assign')")
    public void matchUnmatchedListingToFigurine(@Positive @PathVariable Long unmatchedListingId,
            @Positive @PathVariable Long figurineId) {
        log.info("Matching unmatched listing {} with figurine {}", unmatchedListingId, figurineId);

        figurineStoreService.matchUnmatchedListingToFigurine(unmatchedListingId, figurineId);
    }

    /**
     * Retrieves the average real-time price of a figurine across its matched store
     * listings.
     *
     * @param figurineId
     *            identifier of the canonical figurine
     * @return pricing information calculated from the current store listings
     */
    @GetMapping("/figurines/{figurineId}/average-realtime-price")
    @PreAuthorize("hasAuthority('figurines:stores:read')")
    public FigurineStorePricingResp retrieveAverageRealtimePrice(@Positive @PathVariable Long figurineId) {
        log.info("Retrieving average realtime price for figurine {}", figurineId);

        return figurineStoreService.retrieveAverageRealtimePrice(figurineId);
    }
}
