package com.mesofi.mythclothapi.figurinestores;

import java.util.Currency;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mesofi.mythclothapi.figurines.FigurineService;
import com.mesofi.mythclothapi.figurines.model.Figurine;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class FigurineStoreService {

    private final FigurineService figurineService;
    private final FigurineStoreRepository figurineStoreRepository;
    private final FigurineStorePricingRepository figurineStorePricingRepository;
    private final UnmatchedFigurineListingRepository unmatchedFigurineListingRepository;
    private final StoreRepository storeRepository;

    public void processStorePricing(StoreListing listing) {

        StoreName storeName = listing.store();
        Store store = findOrCreateStore(storeName.name(), storeName.website().toString(), listing.currency());

        figurineService.retrieveFigurineByName(listing.lineUp(), listing.productName()).ifPresentOrElse(figurine -> {
            log.info("[{}] [{}] - {} ==> [{}] - {}", storeName.name(), listing.lineUp(), listing.productName(),
                    figurine.getId(), figurine.getLegacyName());

            FigurineStore figurineStore = findOrCreateFigurineStore(figurine, store, listing.originalProductName(),
                    listing.productName());

            figurineStorePricingRepository.findByFigurineStoreAndCurrentPrice(figurineStore, listing.price())
                    .ifPresentOrElse(
                            figurineStorePricing -> log.warn(
                                    "Pricing {} already exists for figurine '{}' at store '{}'.",
                                    figurineStorePricing.getCurrentPrice(), figurine.getLegacyName(), store.getName()),
                            () -> {
                                FigurineStorePricing pricing = new FigurineStorePricing();
                                pricing.setFigurineStore(figurineStore);
                                pricing.setCurrentPrice(listing.price());
                                figurineStorePricingRepository.save(pricing);

                                log.info("New pricing saved for figurine '{}' at store '{}': {}.",
                                        figurine.getLegacyName(), store.getName(), listing.price());
                            });
        }, () -> {
            log.warn("Figurine not found for name: '{}', this will be resolved manually", listing.productName());

            UnmatchedFigurineListing unmatchedFigurineListing = new UnmatchedFigurineListing();
            unmatchedFigurineListing.setStore(store);
            unmatchedFigurineListing.setOriginalName(listing.originalProductName());
            unmatchedFigurineListing.setNormalizedName(listing.productName());
            unmatchedFigurineListing.setImageUrl(listing.productImageUrl());
            unmatchedFigurineListing.setProductUrl(listing.productUrl());

            unmatchedFigurineListingRepository.save(unmatchedFigurineListing);
        });

    }

    private Store findOrCreateStore(String name, String website, Currency currency) {
        Optional<Store> storeOptional = storeRepository.findByName(name);
        if (storeOptional.isEmpty()) {
            Store store = new Store();
            store.setName(name);
            store.setUrl(website);
            store.setCurrency(currency);
            Store storeSaved = storeRepository.save(store);
            storeOptional = Optional.of(storeSaved);
        }
        return storeOptional.get();
    }

    private FigurineStore findOrCreateFigurineStore(Figurine figurine, Store store, String originalFigurineName,
            String normalizedFigurineName) {

        FigurineStore figurineStore = figurineStoreRepository.findByFigurineAndStore(figurine, store).orElseGet(() -> {
            FigurineStore newFigurineStore = new FigurineStore();
            newFigurineStore.setFigurine(figurine);
            newFigurineStore.setStore(store);
            return newFigurineStore;
        });

        figurineStore.setOriginalName(originalFigurineName);
        figurineStore.setNormalizedName(normalizedFigurineName);

        return figurineStoreRepository.save(figurineStore);
    }
}
