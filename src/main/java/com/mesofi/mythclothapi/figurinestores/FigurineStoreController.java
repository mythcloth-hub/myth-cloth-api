
package com.mesofi.mythclothapi.figurinestores;

import static com.mesofi.mythclothapi.utils.CurrencyConverter.toCurrency;

import java.util.List;

import jakarta.validation.constraints.Positive;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreHistoricalResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreMatchedResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreMatchedSummaryResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStorePriceResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreUnmatchedResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for reviewing and maintaining figurine store listings.
 * <p>
 * Exposes endpoints for matched and unmatched listings, manual assignment,
 * ignored-listing toggling, and real-time pricing lookups.
 * </p>
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/figurine-stores")
@RequiredArgsConstructor
public class FigurineStoreController {

    private final FigurineStoreService figurineStoreService;

    /**
     * Retrieves a summary of matched figurine listings grouped by store.
     *
     * @return summaries containing store information and the number of matched
     *         figurine listings
     */
    @GetMapping("/matched-listings/summary")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('figurines:stores:read')")
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
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('figurines:stores:read')")
    public List<FigurineStoreMatchedResp> retrieveMatchedFigurineListing(@Positive @PathVariable Long storeId) {
        log.info("Retrieving matched figurine listing for store {}", storeId);

        return figurineStoreService.retrieveMatchedFigurineListing(storeId);
    }

    /**
     * Manually unmatches a figurine listing from its associated canonical figurine.
     *
     * @param figurineStoreId
     *            identifier of the matched figurine-store relationship
     */
    @PostMapping("/matched-listings/figurine-store/{figurineStoreId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('figurines:stores:assign')")
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
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('figurines:stores:read')")
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
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('figurines:stores:assign')")
    public void matchUnmatchedListingToFigurine(@Positive @PathVariable Long unmatchedListingId,
            @Positive @PathVariable Long figurineId) {
        log.info("Matching unmatched listing {} with figurine {}", unmatchedListingId, figurineId);

        figurineStoreService.matchUnmatchedListingToFigurine(unmatchedListingId, figurineId);
    }

    /**
     * Marks or unmarks an unmatched figurine listing as ignored.
     *
     * @param unmatchedListingId
     *            identifier of the unmatched listing
     * @param ignored
     *            whether the listing should be ignored
     */
    @PatchMapping("/unmatched-listings/{unmatchedListingId}/ignored/{ignored}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('figurines:stores:assign')")
    public void ignoreUnmatchedFigurineListing(@Positive @PathVariable Long unmatchedListingId,
            @PathVariable boolean ignored) {
        log.info("Setting ignored status of unmatched listing {} to {}", unmatchedListingId, ignored);

        figurineStoreService.ignoreUnmatchedFigurineListing(unmatchedListingId, ignored);
    }

    /**
     * Retrieves the average real-time price of a figurine across its matched store
     * listings.
     *
     * @param figurineId
     *            identifier of the canonical figurine
     * @param currency
     *            optional currency code for price conversion; if not provided, the
     *            default currency of the listings will be used
     * @return pricing information calculated from the current store listings
     */
    @GetMapping("/figurines/{figurineId}/prices/current")
    public FigurineStorePriceResp retrieveAverageRealtimePrice(@Positive @PathVariable Long figurineId,
            @RequestParam(required = false) String currency) {
        log.info("Retrieving average realtime price for figurine {} with currency {}", figurineId, currency);

        return figurineStoreService.retrieveAverageRealtimePrice(figurineId, toCurrency(currency));
    }

    /**
     * Retrieves the historical price history for a figurine.
     * <p>
     * Historical prices can be returned either for all stores or for a specific
     * store. When a {@code storeId} is provided, only prices from that store are
     * returned. Otherwise, prices from all stores are included.
     * <p>
     * If a currency is specified, prices are converted to that currency. When the
     * currency is omitted or invalid, the default currency behavior defined by
     * {@link com.mesofi.mythclothapi.utils.CurrencyConverter#toCurrency(String)} is
     * applied.
     *
     * @param figurineId
     *            the identifier of the figurine
     * @param storeId
     *            the optional store identifier used to filter the results
     * @param currency
     *            the optional ISO 4217 currency code used to convert returned
     *            prices
     * @return the historical pricing information for the requested figurine
     */
    @GetMapping("/figurines/{figurineId}/prices/history")
    public FigurineStoreHistoricalResp retrieveHistoricalPrices(@Positive @PathVariable Long figurineId,
            @Positive @RequestParam(required = false) Long storeId, @RequestParam(required = false) String currency) {
        log.info("Retrieving historical prices for figurine {} with currency {}", figurineId, currency);

        return figurineStoreService.retrieveHistoricalPrices(figurineId, storeId, toCurrency(currency));
    }
}
