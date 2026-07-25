
package com.mesofi.mythclothapi.figurinestores;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesofi.mythclothapi.figurines.FigurineService;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreMatchedResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreMatchedSummaryResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStorePricingResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreUnmatchedResp;
import com.mesofi.mythclothapi.figurinestores.mapper.FigurineStoreMapper;
import com.mesofi.mythclothapi.figurinestores.model.FigurineStore;
import com.mesofi.mythclothapi.figurinestores.model.FigurineStorePricing;
import com.mesofi.mythclothapi.figurinestores.model.UnmatchedFigurineListing;
import com.mesofi.mythclothapi.figurinestores.repository.FigurineStorePricingRepository;
import com.mesofi.mythclothapi.figurinestores.repository.FigurineStoreRepository;
import com.mesofi.mythclothapi.figurinestores.repository.UnmatchedFigurineListingRepository;
import com.mesofi.mythclothapi.messaging.pricing.model.StoreListing;
import com.mesofi.mythclothapi.messaging.pricing.model.StoreName;
import com.mesofi.mythclothapi.stores.StoreRepository;
import com.mesofi.mythclothapi.stores.model.Store;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Coordinates the processing of pricing information received from external
 * stores.
 * <p>
 * For each incoming store listing, this service attempts to resolve the listing
 * to a canonical {@link Figurine}. If a match is found, it creates or updates
 * the corresponding {@link FigurineStore} mapping and records the pricing
 * history. Listings that cannot be resolved automatically are persisted for
 * later manual review.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FigurineStoreService {

    private final FigurineStoreMapper figurineStoreMapper;
    private final FigurineService figurineService;
    private final FigurineRepository figurineRepository;
    private final FigurineStoreRepository figurineStoreRepository;
    private final FigurineStorePricingRepository figurineStorePricingRepository;
    private final UnmatchedFigurineListingRepository unmatchedFigurineListingRepository;
    private final StoreRepository storeRepository;

    /**
     * Processes a pricing update received from an external store.
     * <p>
     * The processing flow is as follows:
     * <ol>
     * <li>Find or create the corresponding {@link Store}.</li>
     * <li>Look for an existing {@link FigurineStore} using the store and the
     * original product name.</li>
     * <li>If a mapping exists, record the pricing if it has not been stored
     * previously.</li>
     * <li>Otherwise, attempt to resolve the listing to a canonical
     * {@link Figurine}.</li>
     * <li>If the figurine is resolved, create or update the store mapping and
     * record the pricing.</li>
     * <li>If no match is found, persist the listing as an
     * {@link UnmatchedFigurineListing} for manual resolution.</li>
     * </ol>
     *
     * @param listing
     *            the pricing information retrieved from a store crawler
     */
    @Transactional
    public void processStorePricing(StoreListing listing) {
        log.info("Processing StoreListing");

        StoreName storeName = listing.store();
        Store store = findOrCreateStore(storeName.name(), storeName.website().toString(), listing.currency());

        figurineStoreRepository.findByStoreAndOriginalName(store, listing.originalProductName()).ifPresentOrElse(
                fs -> createPricingIfAbsent(fs, listing.price(), listing.productName(), store.getName()),
                () -> figurineService.findBestMatchingFigurine(listing.lineUp(), listing.productName()).ifPresentOrElse(
                        figurine -> processMatchedListing(figurine, store, listing),
                        () -> createUnmatchedListing(store, listing)));
    }

    /**
     * Retrieves all unmatched store listings pending manual figurine matching.
     *
     * @return unmatched figurine listing responses
     */
    @Transactional(readOnly = true)
    public List<FigurineStoreUnmatchedResp> retrieveUnmatchedFigurineListings() {
        log.info("Retrieving unmatched figurine listings");

        return unmatchedFigurineListingRepository
                .findAll(Sort.by(Sort.Order.asc("store.id"), Sort.Order.asc("originalName"))).stream()
                .map(figurineStoreMapper::toFigurineStoreUnmatchedResp).toList();
    }

    /**
     * Matches an unmatched store listing to a canonical figurine and removes the
     * listing from the unmatched queue.
     *
     * @param unmatchedFigurineId
     *            unmatched listing identifier
     * @param figurineId
     *            canonical figurine identifier
     */
    @Transactional
    public void matchUnmatchedListingToFigurine(@NotNull Long unmatchedFigurineId, @NotNull Long figurineId) {
        log.info("Matching unmatched figurine listing {} to figurine {}", unmatchedFigurineId, figurineId);

        UnmatchedFigurineListing unmatched = unmatchedFigurineListingRepository.findById(unmatchedFigurineId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unmatched figurine listing not found for ID: " + unmatchedFigurineId));

        Figurine figurine = figurineRepository.findById(figurineId)
                .orElseThrow(() -> new IllegalArgumentException("Figurine not found for ID: " + figurineId));

        Store store = unmatched.getStore();
        StoreListing listing = new StoreListing(null, unmatched.getLineUP(), unmatched.getOriginalName(),
                unmatched.getNormalizedName(), null, null, unmatched.getPrice(), null, null, null, null, null);

        processMatchedListing(figurine, store, listing);

        unmatchedFigurineListingRepository.delete(unmatched);
    }

    @Transactional(readOnly = true)
    public List<FigurineStoreMatchedSummaryResp> retrieveMatchedFigurineListingSummary() {
        log.info("Retrieving matched figurine listing summary");

        List<FigurineStoreMatchedSummaryResp> response = new ArrayList<>();

        for (Store store : storeRepository.findAllByOrderByNameAsc()) {
            long totalFigurines = figurineStoreRepository.countByStore(store);
            response.add(figurineStoreMapper.toFigurineStoreMatchedSummaryResp(store, totalFigurines));
        }
        return response;
    }

    @Transactional(readOnly = true)
    public List<FigurineStoreMatchedResp> retrieveMatchedFigurineListing(@Positive Long storeId) {
        log.info("Retrieving matched figurine listing using storeId {}", storeId);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found for ID: " + storeId));

        return figurineStoreRepository.findByStore(store).stream().map(figurineStore -> {
            String displayableName = figurineService.createDisplayableName(figurineStore.getFigurine());
            return figurineStoreMapper.toFigurineStoreMatchedResp(figurineStore, displayableName);
        }).toList();
    }

    @Transactional(readOnly = true)
    public FigurineStorePricingResp retrieveAverageRealtimePrice(@Positive Long figurineId) {
        log.info("Retrieving average realtime price");

        Figurine figurine = figurineRepository.findById(figurineId)
                .orElseThrow(() -> new IllegalArgumentException("Figurine not found for ID: " + figurineId));

        List<BigDecimal> prices = figurineStoreRepository.findByFigurine(figurine).stream()
                .flatMap(fs -> fs.getPrices().stream()).map(FigurineStorePricing::getCurrentPrice)
                .filter(price -> price != null && price.compareTo(BigDecimal.ZERO) > 0).toList();

        if (prices.isEmpty()) {
            log.info("No pricing data available for figurine {}", figurineId);
            return new FigurineStorePricingResp(BigDecimal.ZERO);
        }

        BigDecimal average = prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP);
        log.info("Average realtime price for figurine {}: {}", figurineId, average);
        return new FigurineStorePricingResp(average);
    }

    /**
     * Processes a store listing that has been successfully matched to a canonical
     * figurine.
     * <p>
     * Ensures that a {@link FigurineStore} mapping exists between the figurine and
     * the store, updates the stored names if necessary, and records the pricing if
     * it has not already been persisted.
     *
     * @param figurine
     *            the resolved canonical figurine
     * @param store
     *            the store where the listing originated
     * @param listing
     *            the scraped store listing
     */
    private void processMatchedListing(Figurine figurine, Store store, StoreListing listing) {
        log.info("[{}] [{}] - {} ==> [{}] - {}", store.getName(), listing.lineUp(), listing.productName(),
                figurine.getId(), figurine.getLegacyName());

        FigurineStore figurineStore = findOrCreateFigurineStore(figurine, store, listing);

        createPricingIfAbsent(figurineStore, listing.price(), listing.productName(), store.getName());
    }

    /**
     * Persists a store listing that could not be matched to any canonical figurine.
     * <p>
     * Duplicate unmatched listings are ignored based on the combination of store
     * and original product name. This allows unresolved listings to be reviewed and
     * manually linked at a later time.
     *
     * @param store
     *            the originating store
     * @param listing
     *            the unmatched store listing
     */
    private void createUnmatchedListing(Store store, StoreListing listing) {
        log.warn("No figurine found for normalized='{}', original='{}'.", listing.productName(),
                listing.originalProductName());

        unmatchedFigurineListingRepository.findByStoreAndOriginalName(store, listing.originalProductName())
                .ifPresentOrElse(existing -> log.warn(
                        "Unmatched figurine listing already exists for original name '{}'. Ignoring duplicate.",
                        existing.getOriginalName()), () -> {
                            UnmatchedFigurineListing unmatched = new UnmatchedFigurineListing();
                            unmatched.setStore(store);
                            unmatched.setLineUP(listing.lineUp());
                            unmatched.setOriginalName(listing.originalProductName());
                            unmatched.setNormalizedName(listing.productName());
                            unmatched.setImageUrl(listing.productImageUrl());
                            unmatched.setProductUrl(listing.productUrl());
                            unmatched.setPrice(listing.price());
                            unmatchedFigurineListingRepository.save(unmatched);
                            log.info("Created unmatched figurine listing '{}'.", listing.originalProductName());
                        });
    }

    /**
     * Records a new pricing entry for a figurine if the same price has not already
     * been stored.
     * <p>
     * Price history is maintained by storing only distinct prices for a
     * {@link FigurineStore}. If the supplied price already exists, no new record is
     * created.
     *
     * @param figurineStore
     *            the figurine-store mapping
     * @param price
     *            the current price
     * @param figurineName
     *            the figurine name used for logging
     * @param storeName
     *            the store name used for logging
     */
    private void createPricingIfAbsent(FigurineStore figurineStore, BigDecimal price, String figurineName,
            String storeName) {
        figurineStorePricingRepository.findByFigurineStoreAndCurrentPrice(figurineStore, price)
                .ifPresentOrElse(p -> log.warn("Pricing {} already exists for figurine '{}' at store '{}'.",
                        p.getCurrentPrice(), figurineName, storeName), () -> {
                            FigurineStorePricing pricing = new FigurineStorePricing();
                            pricing.setFigurineStore(figurineStore);
                            pricing.setCurrentPrice(price);
                            figurineStorePricingRepository.save(pricing);
                            log.info("New pricing saved for figurine '{}' at store '{}': {}.", figurineName, storeName,
                                    price);
                        });
    }

    /**
     * Retrieves an existing store or creates a new one if it has not been
     * registered yet.
     *
     * @param name
     *            the store name
     * @param website
     *            the store website URL
     * @param currency
     *            the currency used by the store
     * @return the existing or newly created store
     */
    private Store findOrCreateStore(String name, String website, Currency currency) {
        return storeRepository.findByName(name).orElseGet(() -> {
            Store store = new Store();
            store.setName(name);
            store.setUrl(website);
            store.setCurrency(currency);
            return storeRepository.save(store);
        });
    }

    /**
     * Retrieves the mapping between a canonical figurine and a store, creating it
     * if it does not already exist.
     * <p>
     * The mapping stores both the original product name provided by the store and
     * its normalized representation used during the matching process. Existing
     * mappings are updated with the latest names before being persisted.
     *
     * @param figurine
     *            the canonical figurine
     * @param store
     *            the associated store
     * @param listing
     *            the store listing containing the store info.
     * @return the existing or newly created figurine-store mapping
     */
    private FigurineStore findOrCreateFigurineStore(Figurine figurine, Store store, StoreListing listing) {

        FigurineStore figurineStore = figurineStoreRepository.findByFigurineAndStore(figurine, store).orElseGet(() -> {
            FigurineStore mapping = new FigurineStore();
            mapping.setFigurine(figurine);
            mapping.setStore(store);
            return mapping;
        });

        figurineStore.setOriginalName(listing.originalProductName());
        figurineStore.setNormalizedName(listing.productName());
        figurineStore.setImageUrl(listing.productImageUrl());
        figurineStore.setProductUrl(listing.productUrl());

        return figurineStoreRepository.save(figurineStore);
    }
}
