package com.mesofi.mythclothapi.collectorspurchases;

import static com.mesofi.mythclothapi.utils.CurrencyConverter.getDefaultCurrency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorNotFoundException;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollectionFigurineService;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseFigurineReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseResp;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseSummaryResp;
import com.mesofi.mythclothapi.collectorspurchases.dto.PurchaseSummaryResp;
import com.mesofi.mythclothapi.collectorspurchases.exceptions.CollectorPurchaseFigurineNotFoundException;
import com.mesofi.mythclothapi.collectorspurchases.exceptions.CollectorPurchaseInvalidShippingStatusException;
import com.mesofi.mythclothapi.collectorspurchases.exceptions.CollectorPurchaseNotFoundException;
import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchase;
import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchaseFigurine;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseTotal;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;
import com.mesofi.mythclothapi.common.BaseId;
import com.mesofi.mythclothapi.common.CurrencyCode;
import com.mesofi.mythclothapi.integration.fix.CurrencyConversionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorPurchaseService {

    public static final String PURCHASES_CACHE = "purchases";
    public static final String PURCHASES_SINGLE_CACHE = "purchases-single";

    private static final Map<Pattern, String> TRACKING_URLS = Map.of(Pattern.compile("(?i)^ups$"),
            "https://www.ups.com/track?tracknum=%s", Pattern.compile("(?i)^dhl$"),
            "https://www.dhl.com/global-en/home/tracking.html?tracking-id=%s", Pattern.compile("(?i)^fed\\s*ex$"),
            "https://www.fedex.com/fedextrack/?trknbr=%s", Pattern.compile("(?i)^correos\\s+de\\s+m[eé]xico$"),
            "https://www.correosdemexico.gob.mx/SSLServicios/SeguimientoEnvio/seguimientoportal2.aspx?guia=%s");

    private final CollectorCollectionFigurineService collectorCollectionFigurineService;
    private final CollectorPurchaseRepository collectorPurchaseRepository;
    private final CurrencyConversionService currencyConversionService;
    private final CollectorPurchaseMapper mapper;

    /**
     * The maximum number of purchases to retrieve for a collector.
     */
    private static final int MAX_PURCHASES = 10;

    /**
     * Creates a new collector purchase for the specified collector.
     *
     * @param collectorId
     *            the identifier of the collector for whom to create the purchase
     * @param collectionId
     *            the identifier of the collection to which the purchase belongs
     * @param request
     *            the request containing the details of the purchase to create
     * @return the response containing the details of the created purchase
     * @throws CollectorNotFoundException
     *             if no collector with the specified identifier exists
     * @throws CollectorPurchaseFigurineNotFoundException
     *             if any of the figurines in the request are not owned by the
     *             collector or do not belong to the same collection
     */
    @Transactional
    @CacheEvict(value = {PURCHASES_CACHE, PURCHASES_SINGLE_CACHE}, allEntries = true)
    public CollectorPurchaseResp createPurchase(Long collectorId, Long collectionId,
            @NotNull @Valid CollectorPurchaseReq request) {
        log.info("Creating collector purchase with order date {}", request.purchaseDate());

        Collector collector = collectorCollectionFigurineService.retrieveCollector(collectorId);
        CollectorCollection collection = collectorCollectionFigurineService.retrieveCollectorCollection(collectionId);

        collectorCollectionFigurineService.ensureCollectionOwnership(collector, collectionId);
        ensureOwnershipAndCollection(collection, request.figurines());

        CollectorPurchase collectorPurchase = mapper.toCollectorPurchase(request);

        // Prepares the purchase figurines by setting the collection figurine references
        // from the collector's collection
        collectorPurchase.setCollector(collector);
        collectorPurchase.setCollection(collection);
        collectorPurchase.getFigurines().forEach(purchaseFigurine -> purchaseFigurine.setPurchase(collectorPurchase));
        updateShippingDates(collectorPurchase);

        var saved = collectorPurchaseRepository.save(collectorPurchase);

        PurchaseTotal purchaseTotal = calculatePurchaseTotal(saved);
        log.info("Saved collector purchase with ID {} and seller '{}'", saved.getId(), saved.getSeller());
        return mapper.toCollectorPurchaseResp(saved, purchaseTotal.totalAmount(), this::generateTrackingUrl);
    }

    /**
     * Updates the shipping dates of the collector purchase based on its shipping
     * status.
     *
     * @param collectorPurchase
     *            the collector purchase to update
     */
    private void updateShippingDates(CollectorPurchase collectorPurchase) {
        if (ShippingStatus.SHIPPED.equals(collectorPurchase.getShippingStatus())) {
            collectorPurchase.setShippedDate(LocalDate.now());
        }
        if (ShippingStatus.DELIVERED.equals(collectorPurchase.getShippingStatus())) {
            collectorPurchase.setDeliveredDate(LocalDate.now());
        }
    }

    /**
     * Retrieves all collector purchases for the specified collector, optionally
     * converting the amounts to the requested currency.
     *
     * @param collectorId
     *            the identifier of the collector for whom to retrieve purchases
     * @param currency
     *            the currency in which to display the purchase amounts (optional)
     * @return a CollectorPurchaseSummaryResp object containing the summary and list
     *         of purchases
     */
    @Transactional(readOnly = true)
    @Cacheable(value = PURCHASES_CACHE, key = "T(java.util.Objects).hash(#collectorId, #currency)")
    public CollectorPurchaseSummaryResp retrievePurchases(Long collectorId, Currency currency) {
        log.info("Retrieving collector purchases for collector ID {} with currency {}", collectorId, currency);

        Collector collector = collectorCollectionFigurineService.retrieveCollector(collectorId);

        List<CollectorPurchase> purchases = collectorPurchaseRepository.findByCollectorOrderByOrderDateAsc(collector,
                PageRequest.of(0, MAX_PURCHASES));

        List<PurchaseTotal> purchaseTotals = purchases.stream().map(cp -> calculatePurchaseTotal(currency, cp))
                .toList();

        Currency summaryCurrency = currency == null
                ? resolveEffectiveCurrency(purchases.stream().map(p -> p.getCurrency().name()).toList())
                : currency;

        BigDecimal grandTotalAmount;
        if (currency == null) {
            grandTotalAmount = purchaseTotals.stream()
                    .map(purchaseTotal -> currencyConversionService.convert(purchaseTotal.totalAmount(),
                            purchaseTotal.currency().getCurrencyCode(), summaryCurrency.getCurrencyCode()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            grandTotalAmount = purchaseTotals.stream().map(PurchaseTotal::totalAmount).reduce(BigDecimal.ZERO,
                    BigDecimal::add);
        }

        PurchaseSummaryResp summary = new PurchaseSummaryResp(summaryCurrency.getCurrencyCode(), grandTotalAmount);
        List<CollectorPurchaseResp> purchaseList = new ArrayList<>();
        for (int i = 0; i < purchases.size(); i++) {
            CollectorPurchaseResp purchaseResp = mapper.toCollectorPurchaseResp(purchases.get(i),
                    purchaseTotals.get(i).totalAmount(), this::generateTrackingUrl);
            purchaseList.add(purchaseResp);
        }

        return new CollectorPurchaseSummaryResp(summary, purchaseList);
    }

    /**
     * Resolves the effective currency to use for the purchases. If a requested
     * currency is provided, it is used. Otherwise, the most frequently used
     * currency among the existing purchases is selected. If no purchases exist, the
     * default currency is returned.
     *
     * @param existingCurrencies
     *            the list of existing currencies from the collector's purchases
     * @return the effective currency to use for the purchases
     */
    private Currency resolveEffectiveCurrency(List<String> existingCurrencies) {

        Map<String, Integer> currencyFrequencies = new HashMap<>();

        for (String currency : existingCurrencies) {
            currencyFrequencies.put(currency, currencyFrequencies.getOrDefault(currency, 0) + 1);
        }

        return currencyFrequencies.entrySet().stream()
                .max(Map.Entry.<String, Integer>comparingByValue()
                        .thenComparing(Map.Entry.comparingByKey(Comparator.reverseOrder())))
                .map(Map.Entry::getKey).map(Currency::getInstance).orElse(getDefaultCurrency());
    }

    /**
     * Calculates the total amount of a collector purchase in its original currency.
     *
     * @param purchase
     *            the collector purchase for which to calculate the total amount
     * @return a PurchaseTotal object containing the total amount and currency
     */
    private PurchaseTotal calculatePurchaseTotal(CollectorPurchase purchase) {
        return calculatePurchaseTotal(null, purchase);
    }

    /**
     * Calculates the total amount of a collector purchase in the requested
     * currency.
     *
     * @param requestedCurrency
     *            the currency in which to calculate the total amount (optional)
     * @param purchase
     *            the collector purchase for which to calculate the total amount
     * @return a PurchaseTotal object containing the total amount and currency
     */
    private PurchaseTotal calculatePurchaseTotal(Currency requestedCurrency, CollectorPurchase purchase) {
        String purchaseCurrency = purchase.getCurrency().name();
        String targetCurrency = requestedCurrency != null ? requestedCurrency.getCurrencyCode() : purchaseCurrency;

        BigDecimal totalAmount = purchase.getFigurines().stream().map(figurine -> {
            BigDecimal effectivePrice = currencyConversionService.convert(figurine.getPricePaid(), purchaseCurrency,
                    targetCurrency);

            figurine.setPricePaid(effectivePrice);

            return effectivePrice.multiply(BigDecimal.valueOf(figurine.getQuantity()));
        }).reduce(BigDecimal.ZERO, BigDecimal::add);

        purchase.setCurrency(CurrencyCode.valueOf(targetCurrency));

        return new PurchaseTotal(Currency.getInstance(targetCurrency), totalAmount);
    }

    /**
     * Retrieves a specific collector purchase for the specified collector.
     *
     * @param collectorId
     *            the identifier of the collector for whom to retrieve the purchase
     * @param purchaseId
     *            the identifier of the purchase to retrieve
     * @return a CollectorPurchaseResp object representing the requested purchase
     * @throws CollectorPurchaseNotFoundException
     *             if no purchase with the specified identifier exists for the
     *             collector
     */
    @Transactional(readOnly = true)
    @Cacheable(value = PURCHASES_SINGLE_CACHE, key = "T(java.util.Objects).hash(#collectorId, #purchaseId, #currency)")
    public CollectorPurchaseResp retrievePurchase(Long collectorId, Long purchaseId, Currency currency) {
        log.info("Retrieving collector purchase with ID {} for collector ID {}", purchaseId, collectorId);

        Collector collector = collectorCollectionFigurineService.retrieveCollector(collectorId);

        CollectorPurchase purchase = collectorPurchaseRepository.findByIdAndCollector(purchaseId, collector)
                .orElseThrow(() -> new CollectorPurchaseNotFoundException(purchaseId));

        PurchaseTotal purchaseTotal = calculatePurchaseTotal(currency, purchase);
        return mapper.toCollectorPurchaseResp(purchase, purchaseTotal.totalAmount(), this::generateTrackingUrl);
    }

    /**
     * Updates an existing collector purchase for the specified collector.
     *
     * @param collectorId
     *            the identifier of the collector for whom to update the purchase
     * @param existingPurchaseId
     *            the identifier of the existing purchase to update
     * @param request
     *            the request containing the updated details of the purchase
     * @return a CollectorPurchaseResp object representing the updated purchase
     * @throws CollectorPurchaseNotFoundException
     *             if no purchase with the specified identifier exists for the
     *             collector
     * @throws CollectorPurchaseFigurineNotFoundException
     *             if any of the figurines in the request are not owned by the
     *             collector or do not belong to the same collection
     */
    @Transactional
    @CacheEvict(value = {PURCHASES_CACHE, PURCHASES_SINGLE_CACHE}, allEntries = true)
    public CollectorPurchaseResp updatePurchase(Long collectorId, Long existingPurchaseId,
            @NotNull @Valid CollectorPurchaseReq request) {
        log.info("Updating collector purchase with ID {} for collector ID {}", existingPurchaseId, collectorId);

        Collector collector = collectorCollectionFigurineService.retrieveCollector(collectorId);
        CollectorPurchase existing = collectorPurchaseRepository.findByIdAndCollector(existingPurchaseId, collector)
                .orElseThrow(() -> new CollectorPurchaseNotFoundException(existingPurchaseId));

        // Update the purchase with the new data from the request, ensuring that the
        // figurines are still owned by the collector and belong to the same collection
        CollectorPurchase incoming = mapper.toCollectorPurchase(request);
        updateShippingDates(incoming);
        mapper.updateCollectorPurchase(existing, incoming);
        // Reconcile the figurines in the purchase with the request
        reconcileFigurines(existing, request.figurines());

        CollectorPurchase saved = collectorPurchaseRepository.saveAndFlush(existing);

        PurchaseTotal purchaseTotal = calculatePurchaseTotal(saved);
        return mapper.toCollectorPurchaseResp(saved, purchaseTotal.totalAmount(), this::generateTrackingUrl);
    }

    /**
     * Updates the shipping status of an existing collector purchase for the
     * specified collector.
     *
     * @param collectorId
     *            the identifier of the collector for whom to update the purchase
     * @param existingPurchaseId
     *            the identifier of the existing purchase to update
     * @param shippingStatus
     *            the new shipping status to set for the purchase
     * @return a CollectorPurchaseResp object representing the updated purchase
     * @throws CollectorPurchaseNotFoundException
     *             if no purchase with the specified identifier exists for the
     *             collector
     * @throws CollectorPurchaseInvalidShippingStatusException
     *             if the provided shipping status is null or invalid
     */
    @Transactional
    @CacheEvict(value = {PURCHASES_CACHE, PURCHASES_SINGLE_CACHE}, allEntries = true)
    public CollectorPurchaseResp updatePurchaseShippingStatus(Long collectorId, Long existingPurchaseId,
            ShippingStatus shippingStatus) {

        Collector collector = collectorCollectionFigurineService.retrieveCollector(collectorId);
        CollectorPurchase existing = collectorPurchaseRepository.findByIdAndCollector(existingPurchaseId, collector)
                .orElseThrow(() -> new CollectorPurchaseNotFoundException(existingPurchaseId));

        ShippingStatus newShippingStatus = Optional.ofNullable(shippingStatus)
                .orElseThrow(CollectorPurchaseInvalidShippingStatusException::new);

        // we only allow updates to the shipping status if it is a valid transition
        existing.setShippingStatus(newShippingStatus);
        updateShippingDates(existing);

        PurchaseTotal purchaseTotal = calculatePurchaseTotal(existing);
        return mapper.toCollectorPurchaseResp(existing, purchaseTotal.totalAmount(), this::generateTrackingUrl);
    }

    /**
     * Reconciles the figurines in the existing purchase with the requested
     * figurines. This method updates existing figurines, removes missing ones, and
     * adds new ones as necessary.
     *
     * @param existingPurchase
     *            the existing collector purchase to reconcile
     * @param requestedFigurines
     *            the list of requested figurines to reconcile with the existing
     *            purchase
     */
    private void reconcileFigurines(CollectorPurchase existingPurchase,
            @NotEmpty List<CollectorPurchaseFigurineReq> requestedFigurines) {

        Map<Long, CollectorPurchaseFigurineReq> requested = requestedFigurines.stream()
                .collect(Collectors.toMap(CollectorPurchaseFigurineReq::collectionFigurineId, Function.identity()));

        // Update existing / remove missing
        existingPurchase.getFigurines().removeIf(existingFigurine -> {
            Long collectionFigurineId = existingFigurine.getCollectionFigurine().getId();
            CollectorPurchaseFigurineReq req = requested.get(collectionFigurineId);

            if (req == null) {
                return true;
            }

            updatePurchaseFigurine(existingFigurine, req);
            return false;
        });

        // Add new
        existingPurchase.getFigurines().stream()
                .map(existingFigurine -> existingFigurine.getCollectionFigurine().getId()).collect(Collectors.toSet())
                .forEach(requested::remove);

        requested.values().forEach(req -> createPurchaseFigurine(existingPurchase, req));
    }

    /**
     * Deletes a specific collector purchase for the specified collector.
     *
     * @param collectorId
     *            the identifier of the collector for whom to delete the purchase
     * @param purchaseId
     *            the identifier of the purchase to delete
     * @throws CollectorPurchaseNotFoundException
     *             if no purchase with the specified identifier exists for the
     *             collector
     */
    @Transactional
    @CacheEvict(value = {PURCHASES_CACHE, PURCHASES_SINGLE_CACHE}, allEntries = true)
    public void deletePurchase(Long collectorId, Long purchaseId) {
        log.info("Deleting collector purchase with ID {}", purchaseId);

        Collector collector = collectorCollectionFigurineService.retrieveCollector(collectorId);
        CollectorPurchase existing = collectorPurchaseRepository.findByIdAndCollector(purchaseId, collector)
                .orElseThrow(() -> new CollectorPurchaseNotFoundException(purchaseId));
        collectorPurchaseRepository.delete(existing);
    }

    /**
     * Creates a new purchase figurine and associates it with the existing purchase.
     *
     * @param existingPurchase
     *            the existing collector purchase to which the new figurine will be
     *            added
     * @param req
     *            the request containing the details of the new purchase figurine
     */
    private void createPurchaseFigurine(CollectorPurchase existingPurchase, CollectorPurchaseFigurineReq req) {
        CollectorPurchaseFigurine incoming = mapper.toCollectorPurchaseFigurine(req);
        incoming.setPurchase(existingPurchase);
        existingPurchase.getFigurines().add(incoming);
    }

    /**
     * Updates an existing purchase figurine with the data from the request.
     *
     * @param existing
     *            the existing purchase figurine to update
     * @param req
     *            the request containing the new data for the purchase figurine
     */
    private void updatePurchaseFigurine(CollectorPurchaseFigurine existing, CollectorPurchaseFigurineReq req) {
        CollectorPurchaseFigurine incoming = mapper.toCollectorPurchaseFigurine(req);
        mapper.updateCollectorPurchaseFigurine(existing, incoming);
    }

    /**
     * Ensures that the collector owns all the figurines in the collection and that
     * they belong to the same collection.
     *
     * @param collection
     *            the collector's collection to check ownership for
     * @param figurines
     *            the list of figurines to check ownership and collection for
     * @throws CollectorPurchaseFigurineNotFoundException
     *             if any of the figurines are not owned by the collector or do not
     *             belong to the same collection
     */
    private void ensureOwnershipAndCollection(CollectorCollection collection,
            List<CollectorPurchaseFigurineReq> figurines) {
        List<Long> figurineIds = figurines.stream().map(CollectorPurchaseFigurineReq::collectionFigurineId).toList();

        log.info("Ensuring ownership and collection for collection figurine ids {}", figurineIds);
        long collectorId = collection.getCollector().getId();

        // finds the figurines in the collector's collections and checks if they belong
        // to the collector
        Set<Long> ownedCollectionFigurineIds = collection.getFigurines().stream()
                .filter(CollectorCollectionFigurine::isOwned).map(BaseId::getId).collect(Collectors.toSet());

        if (ownedCollectionFigurineIds.containsAll(figurineIds)) {
            log.info("All collection figurine IDs {} are owned by collector ID {}", figurineIds, collectorId);
            return;
        }

        log.warn("Collector ID {} does not own all figurine IDs {}", collectorId, figurineIds);
        throw new CollectorPurchaseFigurineNotFoundException(figurineIds);
    }

    /**
     * Generates a tracking URL for the purchase based on the carrier and tracking
     * number.
     *
     * @param purchase
     *            the collector purchase for which to generate the tracking URL
     * @return the tracking URL if both carrier and tracking number are present,
     *         otherwise null
     */
    public String generateTrackingUrl(CollectorPurchase purchase) {
        if (purchase.getCarrier() == null || purchase.getTrackingNumber() == null) {
            return null;
        }

        for (Map.Entry<Pattern, String> entry : TRACKING_URLS.entrySet()) {
            if (entry.getKey().matcher(purchase.getCarrier()).matches()) {
                return entry.getValue().formatted(purchase.getTrackingNumber());
            }
        }
        return null;
    }
}
