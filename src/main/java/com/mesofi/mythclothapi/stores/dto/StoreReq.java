package com.mesofi.mythclothapi.stores.dto;

import java.net.URI;
import java.util.Currency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.mesofi.mythclothapi.messaging.pricing.model.StoreName;

/**
 * Request payload for creating or updating a store.
 *
 * @param name
 *            the display name of the store
 * @param storeName
 *            the predefined store identifier
 * @param website
 *            the store's website URI
 * @param logoUrl
 *            the URI of the store's logo image
 * @param currency
 *            the default currency used by the store
 * @param country
 *            the ISO country code where the store operates
 * @param active
 *            whether the store is active and available for use
 */
public record StoreReq(
        @NotBlank(message = "name must not be blank") @Size(max = 150, message = "store name must not exceed 150 characters") String name,
        @NotNull(message = "store name must not be null") StoreName storeName, @NotNull URI website,
        @NotNull URI logoUrl, @NotNull Currency currency,
        @NotBlank(message = "country must not be blank") @Size(max = 100) String country, boolean active) {
}
