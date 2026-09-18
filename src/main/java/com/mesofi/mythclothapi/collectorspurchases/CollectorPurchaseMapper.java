package com.mesofi.mythclothapi.collectorspurchases;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.function.Function;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseFigurineReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseFigurineResp;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseResp;
import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchaseFigurine;
import com.mesofi.mythclothapi.common.CurrencyCode;

/**
 * Mapper responsible for converting between {@link CollectorPurchaseReq} and
 * {@link CollectorPurchaseFigurineReq} DTOs and their corresponding entity
 * classes, as well as mapping from {@link CollectorPurchase} to
 * {@link CollectorPurchaseResp}.
 *
 * <p>
 * This mapper uses MapStruct to generate the implementation at compile time and
 * is registered as a Spring component.
 */
@Mapper(componentModel = "spring")
public interface CollectorPurchaseMapper {

    /**
     * Converts a {@link CollectorPurchaseReq} into a {@link CollectorPurchase}.
     *
     * <p>
     * The {@code id}, {@code creationDate}, and {@code updateDate} fields are
     * ignored because a new entity is being created. The {@code collector} field is
     * also ignored since it will be set in the service layer. The {@code orderDate}
     * is mapped from the request's {@code purchaseDate}, while the
     * {@code shippedDate} and {@code deliveredDate} are ignored as they will be set
     * later in the purchase lifecycle.
     *
     * @param request
     *            the request DTO containing the purchase data
     * @return a new {@link CollectorPurchase} populated from the request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "collector", ignore = true) // The collector will be set in the service layer
    @Mapping(target = "orderDate", source = "purchaseDate")
    @Mapping(target = "shippedDate", ignore = true)
    @Mapping(target = "deliveredDate", ignore = true)
    CollectorPurchase toCollectorPurchase(CollectorPurchaseReq request);

    /**
     * Converts a {@link CollectorPurchaseFigurineReq} into a
     * {@link CollectorPurchaseFigurine}.
     *
     * <p>
     * The {@code id}, {@code creationDate}, and {@code updateDate} fields are
     * ignored because a new entity is being created. The {@code purchase} and
     * {@code collectionFigurine} fields are also ignored since they will be set in
     * the service layer.
     *
     * @param request
     *            the request DTO containing the figurine data
     * @return a new {@link CollectorPurchaseFigurine} populated from the request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "purchase", ignore = true)
    @Mapping(target = "collectionFigurine", source = "collectionFigurineId")
    CollectorPurchaseFigurine toCollectorPurchaseFigurine(CollectorPurchaseFigurineReq request);

    /**
     * Converts a collection figurine ID into a {@link CollectorCollectionFigurine}.
     *
     * <p>
     * This method creates a new {@link CollectorCollectionFigurine} instance and
     * sets its ID to the provided value. This is useful for mapping the collection
     * figurine ID from a request DTO to the corresponding entity.
     *
     * @param collectionFigurineId
     *            the ID of the collection figurine
     * @return a new {@link CollectorCollectionFigurine} with the specified ID
     */
    default CollectorCollectionFigurine toCollectorCollectionFigurine(long collectionFigurineId) {
        CollectorCollectionFigurine figurine = new CollectorCollectionFigurine();
        figurine.setId(collectionFigurineId);
        return figurine;
    }

    /**
     * Converts a {@link CollectorPurchase} into a {@link CollectorPurchaseResp}.
     *
     * <p>
     * The {@code purchaseId} is mapped from the entity's {@code id}, and the
     * {@code totalAmount} is calculated using the provided function. Other fields
     * are mapped directly from the entity.
     *
     * @param purchase
     *            the entity to be converted
     * @param calculateTotalAmount
     *            a function to calculate the total amount of the purchase
     * @return a new {@link CollectorPurchaseResp} populated from the entity
     */
    @Mapping(target = "purchaseId", source = "id")
    @Mapping(target = "totalAmount", expression = "java(calculateTotalAmount.apply(purchase))")
    CollectorPurchaseResp toCollectorPurchaseResp(CollectorPurchase purchase,
            @Context Function<CollectorPurchase, BigDecimal> calculateTotalAmount);

    /**
     * Converts a {@link CollectorPurchaseFigurine} into a
     * {@link CollectorPurchaseFigurineResp}.
     *
     * <p>
     * The {@code collectionFigurineId} is mapped from the associated
     * {@link CollectorCollectionFigurine}'s {@code id}. Other fields are mapped
     * directly from the entity.
     *
     * @param purchaseFigurine
     *            the entity to be converted
     * @return a new {@link CollectorPurchaseFigurineResp} populated from the entity
     */
    @Mapping(target = "collectionFigurineId", source = "collectionFigurine.id")
    CollectorPurchaseFigurineResp toCollectorPurchaseFigurineResp(CollectorPurchaseFigurine purchaseFigurine);

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
