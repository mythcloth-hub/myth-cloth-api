package com.mesofi.mythclothapi.collectorspurchases;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/purchases")
@RequiredArgsConstructor
public class CollectorPurchaseController {

    private final CollectorPurchaseService collectorPurchaseService;

    @PostMapping
    public CollectorPurchase createPurchase(@RequestBody @Valid CollectorPurchaseReq purchaseRequest) {
        collectorPurchaseService.createPurchase(purchaseRequest);
        return null;
    }
}
