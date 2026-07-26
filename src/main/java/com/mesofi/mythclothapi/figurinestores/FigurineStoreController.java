
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
 * Exposes endpoints for reviewing unmatched store listings and manually
 * matching them to canonical figurines.
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
     * Retrieves unmatched store listings that still require manual matching.
     *
     * @return unmatched listing responses
     */
    @GetMapping("/unmatched-listings")
    @PreAuthorize("hasAuthority('figurines:stores:read')")
    public List<FigurineStoreUnmatchedResp> retrieveUnmatchedFigurineListings() {
        log.info("Retrieving unmatched figurines ...");

        return figurineStoreService.retrieveUnmatchedFigurineListings();
    }

    /**
     * Matches an unmatched listing to a canonical figurine and removes it from the
     * unmatched queue.
     *
     * @param unmatchedListingId
     *            unmatched listing identifier
     * @param figurineId
     *            canonical figurine identifier
     */
    @PostMapping("/unmatched-listings/{unmatchedListingId}/figurines/{figurineId}/match")
    @PreAuthorize("hasAuthority('figurines:stores:assign')")
    public void matchUnmatchedListingToFigurine(@Positive @PathVariable Long unmatchedListingId,
            @Positive @PathVariable Long figurineId) {
        log.info("Matching unmatched listing {} with figurine {}", unmatchedListingId, figurineId);

        figurineStoreService.matchUnmatchedListingToFigurine(unmatchedListingId, figurineId);
    }

    @GetMapping("/matched-listings/summary")
    @PreAuthorize("hasAuthority('figurines:stores:read')")
    public List<FigurineStoreMatchedSummaryResp> retrieveMatchedFigurineListingSummary() {
        log.info("Retrieving matched figurine listing summary ...");

        return figurineStoreService.retrieveMatchedFigurineListingSummary();
    }

    @GetMapping("/matched-listings/stores/{storeId}")
    @PreAuthorize("hasAuthority('figurines:stores:read')")
    public List<FigurineStoreMatchedResp> retrieveMatchedFigurineListing(@Positive @PathVariable Long storeId) {
        log.info("Retrieving matched figurine listing for store {}", storeId);

        return figurineStoreService.retrieveMatchedFigurineListing(storeId);
    }

    @GetMapping("/figurines/{figurineId}/average-realtime-price")
    @PreAuthorize("hasAuthority('figurines:stores:read')")
    public FigurineStorePricingResp retrieveAverageRealtimePrice(@Positive @PathVariable Long figurineId) {
        log.info("Retrieving average realtime price for figurine {}", figurineId);

        return figurineStoreService.retrieveAverageRealtimePrice(figurineId);
    }
}
