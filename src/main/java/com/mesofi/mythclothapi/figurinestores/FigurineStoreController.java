
package com.mesofi.mythclothapi.figurinestores;

import java.util.List;

import jakarta.validation.constraints.Positive;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
public class FigurineStoreController {

    private final FigurineStoreService figurineStoreService;

    /**
     * Retrieves unmatched store listings that still require manual matching.
     *
     * @return unmatched listing responses
     */
    @GetMapping("/unmatched-listings")
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
    public void matchUnmatchedListingToFigurine(@Positive @PathVariable Long unmatchedListingId,
            @Positive @PathVariable Long figurineId) {
        log.info("Matching unmatched listing {} with figurine {}", unmatchedListingId, figurineId);

        figurineStoreService.matchUnmatchedListingToFigurine(unmatchedListingId, figurineId);
    }
}
