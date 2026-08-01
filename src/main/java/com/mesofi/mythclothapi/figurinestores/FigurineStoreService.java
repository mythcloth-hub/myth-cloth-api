
package com.mesofi.mythclothapi.figurinestores;

import static com.mesofi.mythclothapi.utils.CurrencyConverter.isDefaultCurrency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesofi.mythclothapi.figurines.FigurineService;
import com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreHistoricalPriceResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreHistoricalResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreMatchedResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreMatchedSummaryResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStorePriceResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreUnmatchedResp;
import com.mesofi.mythclothapi.figurinestores.mapper.FigurineStoreMapper;
import com.mesofi.mythclothapi.figurinestores.model.CachedStores;
import com.mesofi.mythclothapi.figurinestores.model.FigurineStore;
import com.mesofi.mythclothapi.figurinestores.model.FigurineStorePricing;
import com.mesofi.mythclothapi.figurinestores.model.FigurineStoreUnmatched;
import com.mesofi.mythclothapi.figurinestores.repository.FigurineStorePricingRepository;
import com.mesofi.mythclothapi.figurinestores.repository.FigurineStoreRepository;
import com.mesofi.mythclothapi.figurinestores.repository.UnmatchedFigurineListingRepository;
import com.mesofi.mythclothapi.integration.fix.CurrencyConversionService;
import com.mesofi.mythclothapi.messaging.pricing.model.StoreListing;
import com.mesofi.mythclothapi.messaging.pricing.model.StoreName;
import com.mesofi.mythclothapi.stores.StoreNotFoundException;
import com.mesofi.mythclothapi.stores.StoreRepository;
import com.mesofi.mythclothapi.stores.model.Store;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Coordinates the processing of store listings, figurine-store mappings, and
 * pricing information for figurine-store catalog data.
 * <p>
 * Incoming store listings are resolved against canonical {@link Figurine}
 * records when possible. Matched listings are linked to the corresponding store
 * and pricing entries are persisted. Listings that cannot be resolved are
 * stored for later manual review.
 * <p>
 * The service also exposes operations for reviewing matched and unmatched
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
    private final CurrencyConversionService currencyService;

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
     * {@link FigurineStoreUnmatched} for manual resolution.</li>
     * </ol>
     *
     * @param listing
     *            the pricing information retrieved from a store crawler
     */
    @Transactional
    public void processStorePricing(StoreListing listing) {
        log.info("Processing StoreListing for store: {} ...", listing.store());

        StoreName storeName = listing.store();
        CachedStores cachedStores = findStore(storeName);
        Store store = figurineStoreMapper.toStore(cachedStores);

        // some figurines could not be matched at all with one of the existing figurines
        // in this catalog, so there's no need for the process to try to match again.
        if (unmatchedFigurineListingRepository
                .findByStoreAndOriginalNameAndIgnoredTrue(store, listing.originalProductName()).isPresent()) {
            log.warn("Ignoring StoreListing with StoreName {}, and original name: '{}'", storeName,
                    listing.originalProductName());
            return;
        }

        figurineStoreRepository.findByStoreAndOriginalName(store, listing.originalProductName())
                .ifPresentOrElse(existing -> {
                    existing.setPreorder(listing.preorder());
                    existing.setStatus(listing.status());

                    createOrUpdatePricing(existing, listing.productName(), store.getName(), listing.price(),
                            listing.discount(), listing.checkedAt());
                }, () -> figurineService.findBestMatchingFigurine(listing.lineUp(), listing.productName())
                        .ifPresentOrElse(figurine -> processMatchedListing(figurine, store, listing),
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
     * @throws StoreNotFoundException
     *             if the store does not exist or is inactive
     */
    @Transactional(readOnly = true)
    public List<FigurineStoreMatchedResp> retrieveMatchedFigurineListing(@Positive Long storeId) {
        log.info("Retrieving matched figurine listing using storeId '{}'", storeId);

        Store store = storeRepository.findByIdAndActiveTrue(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));

        List<FigurineStore> figurineStores = figurineStoreRepository.findByStoreOrderByOriginalName(store);

        List<FigurineStoreMatchedResp> figurineStoreMatchedRespList = new ArrayList<>();
        for (FigurineStore figurineStore : figurineStores) {
            List<FigurineStorePricing> pricingList = figurineStorePricingRepository
                    .findByFigurineStoreOrderByCreationDateAsc(figurineStore);

            String displayableName = figurineStore.getFigurine().getDisplayName();

            figurineStoreMatchedRespList.add(figurineStoreMapper.toFigurineStoreMatchedResp(figurineStore,
                    displayableName, Currency.getInstance(store.getCurrency()), pricingList));
        }

        return figurineStoreMatchedRespList;
    }

    /**
     * Converts a matched figurine-store listing back into the unmatched queue.
     *
     * @param figurineStoreId
     *            identifier of the matched figurine-store association
     */
    @Transactional
    public void manuallyUnmatchFigurineListing(@Positive Long figurineStoreId) {
        log.info("Manually unmatching figurine listing for figurine store {}", figurineStoreId);

        FigurineStore figurineStore = figurineStoreRepository.findById(figurineStoreId)
                .orElseThrow(() -> new IllegalArgumentException("FigurineStore not found for ID: " + figurineStoreId));

        List<FigurineStorePricing> pricingList = figurineStorePricingRepository
                .findByFigurineStoreOrderByCreationDateAsc(figurineStore);
        if (pricingList.isEmpty()) {
            throw new IllegalArgumentException("No pricing data found for FigurineStore ID: " + figurineStoreId);
        }

        Store store = figurineStore.getStore();

        StoreListing listing = new StoreListing(null, figurineStore.getLineUP(), figurineStore.getOriginalName(),
                figurineStore.getNormalizedName(), figurineStore.getImageUrl(), figurineStore.getProductUrl(),
                pricingList.getFirst().getCurrentPrice(), pricingList.getFirst().getDiscount(), null,
                Currency.getInstance(store.getCurrency()), figurineStore.getStatus(), figurineStore.isPreorder(),
                pricingList.getFirst().getCheckedAt());

        createUnmatchedListing(store, listing);

        figurineStorePricingRepository.deleteAll(pricingList);
        figurineStoreRepository.delete(figurineStore);
    }

    /**
     * Updates the ignored state of an unmatched figurine listing.
     *
     * @param unmatchedFigurineListingId
     *            identifier of the unmatched listing
     * @param ignored
     *            whether the listing should be ignored
     */
    @Transactional
    public void ignoreUnmatchedFigurineListing(Long unmatchedFigurineListingId, boolean ignored) {
        FigurineStoreUnmatched listing = unmatchedFigurineListingRepository.findById(unmatchedFigurineListingId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unmatched figurine listing not found for ID: " + unmatchedFigurineListingId));

        listing.setIgnored(ignored);

        log.info("Unmatched figurine listing {} has been {}.", unmatchedFigurineListingId,
                ignored ? "ignored" : "unignored");
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

        FigurineStoreUnmatched unmatched = unmatchedFigurineListingRepository.findById(unmatchedFigurineId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unmatched figurine listing not found for ID: " + unmatchedFigurineId));

        Figurine figurine = figurineRepository.findById(figurineId)
                .orElseThrow(() -> new FigurineNotFoundException(figurineId));

        Store store = unmatched.getStore();
        StoreListing listing = new StoreListing(null, unmatched.getLineUP(), unmatched.getOriginalName(),
                unmatched.getNormalizedName(), unmatched.getImageUrl(), unmatched.getProductUrl(), unmatched.getPrice(),
                unmatched.getDiscount(), null, Currency.getInstance(store.getCurrency()), unmatched.getStatus(),
                unmatched.isPreorder(), unmatched.getCheckedAt());

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
     * @param currency
     *            optional currency code for price conversion; if not provided, the
     *            default currency of the listings will be used
     * @return average current price across the figurine's store listings, or zero
     *         when no valid pricing information is available
     * @throws FigurineNotFoundException
     *             if the figurine does not exist
     */
    @Transactional(readOnly = true)
    public FigurineStorePriceResp retrieveAverageRealtimePrice(@Positive Long figurineId, @Nonnull Currency currency) {
        log.info("Retrieving average realtime price for figurine {} with currency {}", figurineId, currency);

        Figurine figurine = figurineRepository.findById(figurineId)
                .orElseThrow(() -> new FigurineNotFoundException(figurineId));

        List<BigDecimal> prices = new ArrayList<>();

        String source;
        String target = currency.getCurrencyCode();

        List<FigurineStore> figurineStores = figurineStoreRepository.findByFigurine(figurine);
        for (FigurineStore figurineStore : figurineStores) {
            source = figurineStore.getStore().getCurrency();

            for (FigurineStorePricing pricing : figurineStore.getPrices()) {
                prices.add(currencyService.convert(pricing.getCurrentPrice(), source, target));
            }
        }

        if (prices.isEmpty()) {
            return new FigurineStorePriceResp(BigDecimal.ZERO, target);
        }

        BigDecimal average = prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP);

        log.info("Average realtime price for figurine {}: {} {}", figurineId, average, target);
        return new FigurineStorePriceResp(average, target);
    }

    /**
     * Retrieves the historical pricing information for a figurine.
     * <p>
     * Historical prices can be retrieved either across all stores or for a specific
     * store. When querying all stores, all prices are converted to the requested
     * currency. When querying a single store, the original store currency is
     * preserved if the requested currency is the default currency; otherwise, all
     * prices are converted to the requested currency.
     * <p>
     * The returned price history is sorted by the time each price was checked, with
     * the most recent entries appearing first.
     *
     * @param figurineId
     *            the identifier of the figurine
     * @param storeId
     *            the identifier of the store to filter by, or {@code null} to
     *            retrieve prices from all stores
     * @param requestedCurrency
     *            the currency in which prices should be returned
     * @return the historical pricing information for the figurine
     * @throws FigurineNotFoundException
     *             if the figurine does not exist
     * @throws StoreNotFoundException
     *             if a non-null store identifier does not correspond to an existing
     *             store
     */
    public FigurineStoreHistoricalResp retrieveHistoricalPrices(@Positive Long figurineId, @Positive Long storeId,
            @Nonnull Currency requestedCurrency) {

        Figurine figurine = figurineRepository.findById(figurineId)
                .orElseThrow(() -> new FigurineNotFoundException(figurineId));

        FigurineStoreHistoricalResp response;
        List<FigurineStoreHistoricalPriceResp> historicalPrices = new ArrayList<>();

        List<FigurineStore> figurineStores = retrieveFigurineStores(figurine, storeId);

        if (storeId == null) {
            for (FigurineStore figurineStore : figurineStores) {
                Store store = figurineStore.getStore();
                for (FigurineStorePricing pricing : figurineStore.getPrices()) {

                    BigDecimal convertedPrice = currencyService.convert(pricing.getCurrentPrice(), store.getCurrency(),
                            requestedCurrency.getCurrencyCode());

                    historicalPrices.add(new FigurineStoreHistoricalPriceResp(store.getName(), store.getLogoUrl(),
                            figurineStore.getProductUrl(), convertedPrice, pricing.getCheckedAt()));
                }
            }
            response = new FigurineStoreHistoricalResp(figurine.getNormalizedName(),
                    requestedCurrency.getCurrencyCode(), historicalPrices);

        } else {
            String currencyCode = null;

            for (FigurineStore figurineStore : figurineStores) {
                Store store = figurineStore.getStore();
                for (FigurineStorePricing pricing : figurineStore.getPrices()) {
                    BigDecimal convertedPrice;

                    if (isDefaultCurrency(requestedCurrency)) {
                        currencyCode = store.getCurrency();
                        convertedPrice = pricing.getCurrentPrice();

                    } else {
                        currencyCode = requestedCurrency.getCurrencyCode();
                        convertedPrice = currencyService.convert(pricing.getCurrentPrice(), store.getCurrency(),
                                currencyCode);

                    }
                    historicalPrices.add(new FigurineStoreHistoricalPriceResp(store.getName(), store.getLogoUrl(),
                            figurineStore.getProductUrl(), convertedPrice, pricing.getCheckedAt()));
                }
            }
            response = new FigurineStoreHistoricalResp(figurine.getNormalizedName(), currencyCode, historicalPrices);
        }

        historicalPrices.sort(Comparator.comparing(FigurineStoreHistoricalPriceResp::checkedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return response;
    }

    /**
     * Retrieves the {@link FigurineStore} associations for the specified figurine.
     * <p>
     * If no store identifier is provided, all stores associated with the figurine
     * are returned. Otherwise, only the association for the specified store is
     * retrieved.
     *
     * @param figurine
     *            the figurine whose store associations should be retrieved
     * @param storeId
     *            the identifier of the store to filter by, or {@code null} to
     *            retrieve all associated stores
     * @return the matching figurine-store associations
     * @throws StoreNotFoundException
     *             if the specified store does not exist
     */
    private List<FigurineStore> retrieveFigurineStores(Figurine figurine, Long storeId) {
        if (storeId == null) {
            return figurineStoreRepository.findByFigurine(figurine);
        }

        Store store = storeRepository.findById(storeId).orElseThrow(() -> new StoreNotFoundException(storeId));
        return figurineStoreRepository.findByFigurineAndStore(figurine, store);
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
                figurine.getId(), figurine.getNormalizedName());

        FigurineStore figurineStore = findOrCreateFigurineStore(figurine, store, listing);

        createOrUpdatePricing(figurineStore, listing.productName(), store.getName(), listing.price(),
                listing.discount(), listing.checkedAt());
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

        unmatchedFigurineListingRepository
                .findByStoreAndOriginalNameAndIgnoredFalse(store, listing.originalProductName())
                .ifPresentOrElse(existing -> log.warn(
                        "Unmatched figurine listing already exists for original name '{}'. Ignoring duplicate.",
                        existing.getOriginalName()), () -> {
                            FigurineStoreUnmatched unmatched = new FigurineStoreUnmatched();
                            unmatched.setStore(store);
                            unmatched.setLineUP(listing.lineUp());
                            unmatched.setOriginalName(listing.originalProductName());
                            unmatched.setNormalizedName(listing.productName());
                            unmatched.setImageUrl(listing.productImageUrl());
                            unmatched.setProductUrl(listing.productUrl());
                            unmatched.setPrice(listing.price());
                            unmatched.setDiscount(listing.discount());
                            unmatched.setStatus(listing.status());
                            unmatched.setPreorder(listing.preorder());
                            unmatched.setCheckedAt(listing.checkedAt());
                            unmatched.setIgnored(false);
                            unmatchedFigurineListingRepository.save(unmatched);
                            log.info("Created unmatched figurine listing '{}'.", listing.originalProductName());
                        });
    }

    /**
     * Records a new pricing entry for a figurine or updates an existing one if the
     * price has changed.
     * <p>
     * Price history is maintained by storing only distinct prices for a
     * {@link FigurineStore}. If the supplied price already exists, the existing
     * record is updated.
     *
     * @param figurineStore
     *            the figurine-store mapping
     * @param figurineName
     *            the figurine name used for logging
     * @param storeName
     *            the store name used for logging
     * @param price
     *            the current price
     * @param discount
     *            the current discount, if any
     * @param checkedAt
     *            the timestamp when the price was checked
     */
    private void createOrUpdatePricing(FigurineStore figurineStore, String figurineName, String storeName,
            BigDecimal price, BigDecimal discount, Instant checkedAt) {

        figurineStorePricingRepository.findByFigurineStoreAndCurrentPrice(figurineStore, price)
                .ifPresentOrElse(existing -> {
                    existing.setCheckedAt(checkedAt);
                    existing.setDiscount(discount);

                    log.info("Updated pricing for figurine '{}' at store '{}': {}.", figurineName, storeName, price);
                }, () -> {
                    FigurineStorePricing pricing = new FigurineStorePricing();
                    pricing.setFigurineStore(figurineStore);
                    pricing.setCurrentPrice(price);
                    pricing.setDiscount(discount);
                    pricing.setCheckedAt(checkedAt);

                    figurineStorePricingRepository.save(pricing);

                    log.info("Created pricing for figurine '{}' at store '{}': {}.", figurineName, storeName, price);
                });
    }

    /**
     * Retrieves the cached store metadata for the specified store.
     * <p>
     * If the store cache has not yet been initialized, all active stores are loaded
     * from the database, mapped to {@link CachedStores} instances, and stored in
     * the cache for subsequent lookups.
     *
     * @param storeName
     *            the store identifier to retrieve
     * @return the cached store metadata
     * @throws IllegalArgumentException
     *             if no active store exists for the specified store identifier
     * @throws NullPointerException
     *             if the configured store cache is unavailable
     */
    private CachedStores findStore(StoreName storeName) {
        Cache cache = Objects.requireNonNull(cacheManager.getCache(STORE_CACHE), "stores cache not configured");

        @SuppressWarnings("unchecked")
        List<CachedStores> cachedStores = cache.get(STORE_KEY, List.class);

        if (cachedStores == null) {
            List<Store> allStores = storeRepository.findAllByActiveTrueOrderByNameAsc();
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

        FigurineStore figurineStore = figurineStoreRepository
                .findByFigurineAndStoreAndOriginalName(figurine, store, listing.originalProductName()).orElseGet(() -> {
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
        figurineStore.setStatus(listing.status());
        figurineStore.setPreorder(listing.preorder());

        return figurineStoreRepository.save(figurineStore);
    }
}
