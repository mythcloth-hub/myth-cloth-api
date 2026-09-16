package com.mesofi.mythclothapi.collectorspurchases;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.function.Function;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseResp;
import com.mesofi.mythclothapi.common.CurrencyCode;

@Mapper(componentModel = "spring")
public interface CollectorPurchaseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "orderDate", source = "purchaseDate")
    @Mapping(target = "shippedDate", ignore = true)
    @Mapping(target = "deliveredDate", ignore = true)
    CollectorPurchase toCollectorPurchase(CollectorPurchaseReq request);

    @Mapping(target = "purchaseId", source = "id")
    @Mapping(target = "totalAmount", expression = "java(calculateTotalAmount.apply(purchase))")
    CollectorPurchaseResp toCollectorPurchaseResp(CollectorPurchase purchase,
            @Context Function<CollectorPurchase, BigDecimal> calculateTotalAmount);

    /**
     * Maps a {@link Currency} object to a {@link CurrencyCode} enum.
     *
     * @param currency
     *            the {@link Currency} object to be mapped
     * @return the corresponding {@link CurrencyCode} enum, or null if the input is
     *         null
     */
    default CurrencyCode mapCurrencyCode(Currency currency) {
        if (currency == null) {
            return null;
        }
        return CurrencyCode.valueOf(currency.getCurrencyCode());
    }
}
