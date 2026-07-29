package com.mesofi.mythclothapi.utils;

import java.util.Currency;

import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for converting ISO 4217 currency codes to {@link Currency}
 * instances.
 *
 * <p>
 * If the provided currency code is null, empty, or invalid, the default
 * currency ({@code JPY}) is returned.
 * </p>
 */
@Slf4j
public final class CurrencyConverter {

    private static final Currency DEFAULT = Currency.getInstance("JPY");

    private CurrencyConverter() {
        // Utility class
    }

    /**
     * Converts the specified currency code to a {@link Currency}.
     *
     * <p>
     * If the currency code is null, empty, or does not represent a valid ISO 4217
     * currency code, this method logs a warning and returns the default currency.
     * </p>
     *
     * @param currencyCode
     *            the ISO 4217 currency code to convert
     * @return the corresponding {@link Currency}, or {@code JPY} when the code is
     *         null, empty, or invalid
     */
    public static Currency toCurrency(String currencyCode) {
        if (!StringUtils.hasLength(currencyCode)) {
            return DEFAULT;
        }

        try {
            return Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            log.warn("Unable to convert {} to Currency", currencyCode);
            return DEFAULT;
        }
    }

    public static boolean isDefault(Currency currency) {
        return DEFAULT.equals(currency);
    }
}
