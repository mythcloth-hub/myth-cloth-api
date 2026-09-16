package com.mesofi.mythclothapi.collectorspurchases;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void createPurchase(@NotNull @Valid CollectorPurchaseReq request) {
        log.info("Creating collector purchase with order date {}", request.purchaseDate());

        CollectorPurchase collectorPurchase = mapper.toCollectorPurchase(request);

        var saved = collectorPurchaseRepository.save(collectorPurchase);
        log.info("Saved collector purchase {}", saved);
    }
}
