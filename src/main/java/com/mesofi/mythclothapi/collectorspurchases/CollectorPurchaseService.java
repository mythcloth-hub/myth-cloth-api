package com.mesofi.mythclothapi.collectorspurchases;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
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

        if (ShippingStatus.SHIPPED.equals(collectorPurchase.getShippingStatus())) {
            collectorPurchase.setShippedDate(LocalDate.now());
        }
        if (ShippingStatus.DELIVERED.equals(collectorPurchase.getShippingStatus())) {
            collectorPurchase.setDeliveredDate(LocalDate.now());
        }

        var saved = collectorPurchaseRepository.save(collectorPurchase);

        log.info("Saved collector purchase with ID {} and seller '{}'", saved.getId(), saved.getSeller());
        return mapper.toCollectorPurchaseResp(saved, this::calculateTotalAmount);
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
                .orElseThrow(() -> new CollectorPurchaseNotFoundException(collectorId));
        return mapper.toCollectorPurchaseResp(purchase, this::calculateTotalAmount);
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
