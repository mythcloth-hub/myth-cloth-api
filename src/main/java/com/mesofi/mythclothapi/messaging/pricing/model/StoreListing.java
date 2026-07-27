package com.mesofi.mythclothapi.messaging.pricing.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

/**
 * Normalized store listing produced by the crawler pipeline.
 *
 * @param store
 *            the source store from which the listing was retrieved.
 * @param lineUp
 *            the detected Myth Cloth line-up, or {@code null} if it could not
 *            be determined.
 * @param originalProductName
 *            the original product name extracted from the store without
 *            normalization.
 * @param productName
 *            the normalized product name used for matching and processing.
 * @param productImageUrl
 *            the URL of the product image.
 * @param productUrl
 *            the URL of the product page.
 * @param price
 *            the product price.
 * @param discount
 *            the discount percentage applied to the product, or {@code null} if
 *            no discount is available.
 * @param discountedPrice
 *            the calculated discounted price, or {@code null} if no discount
 *            applies.
 * @param currency
 *            the currency associated with the product price.
 * @param status
 *            the normalized availability status of the listing.
 * @param checkedAt
 *            the timestamp when the listing was crawled.
 */
public record StoreListing(StoreName store, LineUP lineUp, String originalProductName, String productName,
        String productImageUrl, String productUrl, BigDecimal price, BigDecimal discount, BigDecimal discountedPrice,
        Currency currency, ListingStatus status, Instant checkedAt) {
}
