package com.mesofi.mythclothapi.collectorspurchases;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectors.CollectorRepository;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorNotFoundException;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseFigurineReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseResp;
import com.mesofi.mythclothapi.collectorspurchases.exceptions.CollectorPurchaseFigurineNotFoundException;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;
import com.mesofi.mythclothapi.collectorspurchases.repository.CollectorPurchaseRepository;
import com.mesofi.mythclothapi.common.BaseId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorPurchaseService {

    private final CollectorPurchaseRepository collectorPurchaseRepository;
    private final CollectorRepository collectorRepository;
    private final CollectorPurchaseMapper mapper;

    /**
     * Creates a new collector purchase for the specified collector.
     *
     * @param collectorId
     *            the identifier of the collector for whom to create the purchase
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
    public CollectorPurchaseResp createPurchase(Long collectorId, @NotNull @Valid CollectorPurchaseReq request) {
        log.info("Creating collector purchase with order date {}", request.purchaseDate());

        Collector collector = retrieveCollector(collectorId);
        ensureOwnershipAndCollection(collector,
                request.figurines().stream().map(CollectorPurchaseFigurineReq::collectionFigurineId).toList());

        CollectorPurchase collectorPurchase = mapper.toCollectorPurchase(request);

        collectorPurchase.setCollector(collector);
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
     * Ensures that the collector owns all the figurines in the collection and that
     * they belong to the same collection.
     *
     * @param collector
     *            the collector to check ownership for
     * @param collectionFigurineIds
     *            the list of figurine IDs to check ownership and collection for
     * @throws CollectorPurchaseFigurineNotFoundException
     *             if any of the figurines are not owned by the collector or do not
     *             belong to the same collection
     */
    private void ensureOwnershipAndCollection(Collector collector, List<Long> collectionFigurineIds) {
        log.info("Ensuring ownership and collection for figurine IDs {}", collectionFigurineIds);

        // finds the figurines in the collector's collections and checks if they belong
        // to the collector
        for (CollectorCollection collection : collector.getCollections()) {
            Set<Long> ownedFigurineIds = collection.getFigurines().stream().filter(CollectorCollectionFigurine::isOwned)
                    .map(CollectorCollectionFigurine::getFigurine).map(BaseId::getId).collect(Collectors.toSet());

            if (ownedFigurineIds.containsAll(collectionFigurineIds)) {
                log.info("All figurine IDs {} are owned by collector ID {}", collectionFigurineIds, collector.getId());
                return;
            }
        }

        log.warn("Collector ID {} does not own all figurine IDs {}", collector.getId(), collectionFigurineIds);
        throw new CollectorPurchaseFigurineNotFoundException(collectionFigurineIds);
    }

    /**
     * Calculates the total amount of a collector purchase based on the associated
     * figurines and their prices.
     *
     * @param purchase
     *            the collector purchase for which to calculate the total amount
     * @return the total amount as a BigDecimal, or BigDecimal.ZERO if the purchase
     *         is null
     */
    public BigDecimal calculateTotalAmount(CollectorPurchase purchase) {
        // TODO: Implement the logic to calculate the total amount based on the
        // associated figurines and their prices.
        return BigDecimal.ONE;
    }

    /**
     * Retrieves a collector by its identifier.
     *
     * @param collectorId
     *            the identifier of the collector to retrieve
     * @return the collector with the specified identifier
     * @throws CollectorNotFoundException
     *             if no collector with the specified identifier exists
     */
    private Collector retrieveCollector(Long collectorId) {
        return collectorRepository.findById(collectorId).orElseThrow(() -> new CollectorNotFoundException(collectorId));
    }
}
