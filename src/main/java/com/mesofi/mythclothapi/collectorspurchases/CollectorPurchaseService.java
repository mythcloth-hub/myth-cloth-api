package com.mesofi.mythclothapi.collectorspurchases;

import org.springframework.stereotype.Service;

import com.mesofi.mythclothapi.collectorspurchases.repository.CollectorPurchaseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorPurchaseService {

    private final CollectorPurchaseRepository collectorPurchaseRepository;
    private final CollectorPurchaseMapper mapper;

    public void createPurchase() {
        log.info("Creating a new collector purchase");

        CollectorPurchase collectorPurchase = mapper.toCollectorPurchase(new CollectorPurchaseReq());

        var saved = collectorPurchaseRepository.save(collectorPurchase);
    }
}
