package com.mesofi.mythclothapi.collectorspurchases;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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
import com.mesofi.mythclothapi.collectorspurchases.exceptions.CollectorPurchaseFigurineNotFoundException;
import com.mesofi.mythclothapi.collectorspurchases.exceptions.CollectorPurchaseNotFoundException;
import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchaseFigurine;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;
import com.mesofi.mythclothapi.collectorspurchases.repository.CollectorPurchaseRepository;
import com.mesofi.mythclothapi.common.BaseId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorPurchaseService {

    private final CollectorCollectionFigurineService collectorCollectionFigurineService;
    private final CollectorPurchaseRepository collectorPurchaseRepository;
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
        collectorPurchase.getFigurines().forEach(purchaseFigurine -> purchaseFigurine.setPurchase(collectorPurchase));
        updateShippingDates(collectorPurchase);

        var saved = collectorPurchaseRepository.save(collectorPurchase);

        log.info("Saved collector purchase with ID {} and seller '{}'", saved.getId(), saved.getSeller());
        return mapper.toCollectorPurchaseResp(saved, this::calculateTotalAmount);
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
     * Retrieves all collector purchases for the specified collector.
     *
     * @param collectorId
     *            the identifier of the collector for whom to retrieve purchases
     * @return a list of CollectorPurchaseResp objects representing the collector's
     *         purchases
     */
    @Transactional(readOnly = true)
    public List<CollectorPurchaseResp> retrievePurchases(Long collectorId) {
        log.info("Retrieving collector purchases for collector ID {}", collectorId);

        Collector collector = collectorCollectionFigurineService.retrieveCollector(collectorId);

        return collectorPurchaseRepository
                .findByCollectorOrderByOrderDateDesc(collector, PageRequest.of(0, MAX_PURCHASES)).stream()
                .map(purchase -> mapper.toCollectorPurchaseResp(purchase, this::calculateTotalAmount)).toList();
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
    public CollectorPurchaseResp retrievePurchase(Long collectorId, Long purchaseId) {
        log.info("Retrieving collector purchase with ID {} for collector ID {}", purchaseId, collectorId);

        Collector collector = collectorCollectionFigurineService.retrieveCollector(collectorId);

        CollectorPurchase purchase = collectorPurchaseRepository.findByIdAndCollector(purchaseId, collector)
                .orElseThrow(() -> new CollectorPurchaseNotFoundException(purchaseId));
        return mapper.toCollectorPurchaseResp(purchase, this::calculateTotalAmount);
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
        return mapper.toCollectorPurchaseResp(saved, this::calculateTotalAmount);
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
     * Calculates the total amount of the purchase based on the figurines and their
     * prices.
     *
     * @param purchase
     *            the collector purchase for which to calculate the total amount
     * @return the total amount of the purchase
     */
    public BigDecimal calculateTotalAmount(CollectorPurchase purchase) {
        return purchase.getFigurines().stream().map(purchaseFigurine -> {
            BigDecimal unitPrice = purchaseFigurine.getPricePaid();
            int quantity = purchaseFigurine.getQuantity();
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
