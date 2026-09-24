package com.mesofi.mythclothapi.collectorspurchases.model;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Represents the total amount of purchases in a specific currency.
 *
 * @param currency
 *            The currency of the total amount.
 * @param totalAmount
 *            The total amount of purchases.
 */
public record PurchaseTotal(Currency currency, BigDecimal totalAmount) {
}
