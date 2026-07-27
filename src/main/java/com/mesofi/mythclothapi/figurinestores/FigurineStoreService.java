
package com.mesofi.mythclothapi.figurinestores;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
import com.mesofi.mythclothapi.figurinestores.model.CachedStores;
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
 * Coordinates the processing of store listings, figurine-store mappings, and
 * pricing information received from external store crawlers.
 * <p>
 * For each incoming store listing, this service attempts to resolve the listing
 * to a canonical {@link Figurine}. Successfully matched listings are associated
 * with the corresponding store and figurine, and their pricing information is
 * recorded. Listings that cannot be resolved automatically are persisted for
 * later manual review.
 * <p>
 * The service also provides operations for reviewing matched and unmatched
 * listings, manually matching unresolved listings, and retrieving current
 * pricing information.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FigurineStoreService {

    private static final String STORE_CACHE = "stores";
    private static final String STORE_KEY = "store";

    private final FigurineStoreMapper figurineStoreMapper;
    private final FigurineService figurineService;
    private final FigurineRepository figurineRepository;
    private final FigurineStoreRepository figurineStoreRepository;
    private final FigurineStorePricingRepository figurineStorePricingRepository;
    private final UnmatchedFigurineListingRepository unmatchedFigurineListingRepository;
    private final StoreRepository storeRepository;
    private final CacheManager cacheManager;

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
        log.info("Processing StoreListing ...");

        StoreName storeName = listing.store();
        CachedStores cachedStores = findStore(storeName);
        Store store = figurineStoreMapper.toStore(cachedStores);

        figurineStoreRepository.findByStoreAndOriginalName(store, listing.originalProductName()).ifPresentOrElse(
                fs -> createPricingIfAbsent(fs, listing.price(), listing.productName(), store.getName()),
                () -> figurineService.findBestMatchingFigurine(listing.lineUp(), listing.productName()).ifPresentOrElse(
                        figurine -> processMatchedListing(figurine, store, listing),
                        () -> createUnmatchedListing(store, listing)));
    }

    /**
     * Retrieves a summary of matched figurine listings grouped by active store.
     * <p>
     * Stores are returned in alphabetical order by name, with each summary
     * containing the number of figurines currently associated with the store.
     *
     * @return summaries of matched figurine listings for all active stores
     */
    @Transactional(readOnly = true)
    public List<FigurineStoreMatchedSummaryResp> retrieveMatchedFigurineListingSummary() {
        log.info("Retrieving matched figurine listing summary");

        List<FigurineStoreMatchedSummaryResp> response = new ArrayList<>();

        for (Store store : storeRepository.findAllByActiveTrueOrderByNameAsc()) {
            long totalFigurines = figurineStoreRepository.countByStore(store);
            response.add(figurineStoreMapper.toFigurineStoreMatchedSummaryResp(store, totalFigurines));
        }
        return response;
    }

    /**
     * Retrieves all figurine-store mappings associated with an active store.
     * <p>
     * Each mapping is converted into a response containing the store listing
     * information and a displayable name for the associated canonical figurine.
     *
     * @param storeId
     *            identifier of the store
     * @return matched figurine listings associated with the store
     * @throws IllegalArgumentException
     *             if the store does not exist or is inactive
     */
    @Transactional(readOnly = true)
    public List<FigurineStoreMatchedResp> retrieveMatchedFigurineListing(@Positive Long storeId) {
        log.info("Retrieving matched figurine listing using storeId {}", storeId);

        Store store = storeRepository.findByIdAndActiveTrue(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found for ID: " + storeId));

        return figurineStoreRepository.findByStore(store).stream().map(figurineStore -> {
            String displayableName = figurineService.createDisplayableName(figurineStore.getFigurine());
            return figurineStoreMapper.toFigurineStoreMatchedResp(figurineStore, displayableName);
        }).toList();
    }

    @Transactional
    public void manuallyUnmatchFigurineListing(@Positive Long figurineStoreId) {
        log.info("Manually unmatching figurine listing for figurine store {}", figurineStoreId);

        FigurineStore figurineStore = figurineStoreRepository.findById(figurineStoreId)
                .orElseThrow(() -> new IllegalArgumentException("FigurineStore not found for ID: " + figurineStoreId));

        List<FigurineStorePricing> pricingList = figurineStorePricingRepository
                .findByFigurineStoreOrderByCreationDateDesc(figurineStore);
        if (pricingList.isEmpty()) {
            throw new IllegalArgumentException("No pricing data found for FigurineStore ID: " + figurineStoreId);
        }

        Store store = figurineStore.getStore();

        StoreListing listing = new StoreListing(null, figurineStore.getLineUP(), figurineStore.getOriginalName(),
                figurineStore.getNormalizedName(), figurineStore.getImageUrl(), figurineStore.getProductUrl(),
                pricingList.getFirst().getCurrentPrice(), null, null, null, null, null);

        createUnmatchedListing(store, listing);

        figurineStorePricingRepository.deleteAll(pricingList);
        figurineStoreRepository.delete(figurineStore);
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
     * Manually matches an unmatched store listing to a canonical figurine.
     * <p>
     * The existing unmatched listing is converted into a matched
     * {@link FigurineStore} association, its pricing information is recorded, and
     * the listing is subsequently removed from the unmatched queue.
     *
     * @param unmatchedFigurineId
     *            identifier of the unmatched store listing
     * @param figurineId
     *            identifier of the canonical figurine
     * @throws IllegalArgumentException
     *             if either the unmatched listing or canonical figurine does not
     *             exist
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

    /**
     * Retrieves the average current price of a figurine across its associated store
     * listings.
     * <p>
     * Only positive current prices are included in the calculation. The resulting
     * average is rounded to two decimal places using {@link RoundingMode#HALF_UP}.
     * If the figurine has no valid pricing information, an average price of zero is
     * returned.
     *
     * @param figurineId
     *            identifier of the canonical figurine
     * @return average current price across the figurine's store listings, or zero
     *         when no valid pricing information is available
     * @throws IllegalArgumentException
     *             if the figurine does not exist
     */
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
     *            listing the store listing containing the store info.
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

    private CachedStores findStore(StoreName storeName) {
        Cache cache = Objects.requireNonNull(cacheManager.getCache(STORE_CACHE), "stores cache not configured");

        @SuppressWarnings("unchecked")
        List<CachedStores> cachedStores = cache.get(STORE_KEY, List.class);

        if (cachedStores == null) {
            List<Store> allStores = storeRepository.findAllByActiveTrue();
            cachedStores = allStores.stream().map(figurineStoreMapper::toStoreCache).toList();

            cache.put(STORE_KEY, cachedStores);
        }
        return cachedStores.stream().filter(cs -> cs.code().equals(storeName.name())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Store not found for code: " + storeName.name()));
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

        figurineStore.setLineUP(listing.lineUp());
        figurineStore.setOriginalName(listing.originalProductName());
        figurineStore.setNormalizedName(listing.productName());
        figurineStore.setImageUrl(listing.productImageUrl());
        figurineStore.setProductUrl(listing.productUrl());

        return figurineStoreRepository.save(figurineStore);
    }
}
