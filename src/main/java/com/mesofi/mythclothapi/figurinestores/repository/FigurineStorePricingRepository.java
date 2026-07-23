package com.mesofi.mythclothapi.figurinestores.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.figurinestores.model.FigurineStore;
import com.mesofi.mythclothapi.figurinestores.model.FigurineStorePricing;

@Repository
public interface FigurineStorePricingRepository extends JpaRepository<FigurineStorePricing, Long> {
    Optional<FigurineStorePricing> findByFigurineStoreAndCurrentPrice(FigurineStore figurineStore,
            BigDecimal currentPrice);
}
