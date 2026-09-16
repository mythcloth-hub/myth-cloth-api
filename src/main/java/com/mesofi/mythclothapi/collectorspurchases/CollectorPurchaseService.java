package com.mesofi.mythclothapi.collectorspurchases;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseResp;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;
import com.mesofi.mythclothapi.collectorspurchases.repository.CollectorPurchaseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorPurchaseService {

    private final CollectorPurchaseRepository collectorPurchaseRepository;
    private final CollectorPurchaseMapper mapper;

    @Transactional
    public CollectorPurchaseResp createPurchase(@NotNull @Valid CollectorPurchaseReq request) {
        log.info("Creating collector purchase with order date {}", request.purchaseDate());

        CollectorPurchase collectorPurchase = mapper.toCollectorPurchase(request);
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
     * Calculates the total amount of a collector purchase based on the associated
     * figurines and their prices.
     *
     * @param purchase
     *            the collector purchase for which to calculate the total amount
     * @return the total amount as a BigDecimal, or BigDecimal.ZERO if the purchase
     *         is null
     */
    public BigDecimal calculateTotalAmount(CollectorPurchase purchase) {
        if (purchase == null) {
            return BigDecimal.ZERO;
        }
        // TODO: Implement the logic to calculate the total amount based on the
        // associated figurines and their prices.
        return BigDecimal.ONE;
    }
}
