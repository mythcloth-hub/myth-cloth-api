package com.mesofi.mythclothapi.stores.dto;

/**
 * Response containing store information.
 *
 * @param id
 *            the unique identifier of the store
 * @param name
 *            the display name of the store
 * @param storeName
 *            the predefined store identifier
 * @param website
 *            the store's website URL
 * @param logoUrl
 *            the URL of the store's logo image
 * @param currency
 *            the default currency used by the store
 * @param country
 *            the ISO country code where the store operates
 * @param active
 *            whether the store is active
 */
public record StoreResp(long id, String name, String storeName, String website, String logoUrl, String currency,
        String country, boolean active) {
}
