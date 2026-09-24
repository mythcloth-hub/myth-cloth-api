package com.mesofi.mythclothapi.utils;

import java.util.Currency;

import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for converting ISO 4217 currency codes to {@link Currency}
 * instances.
 * <p>
 * If the provided currency code is {@code null}, empty, or invalid, the default
 * currency ({@code JPY}) is returned.
 */
@Slf4j
public final class CurrencyConverter {

    /**
     * Default currency returned when a conversion cannot be performed.
     */
    private static final Currency DEFAULT = Currency.getInstance("JPY");

    /**
     * Converts the specified ISO 4217 currency code to a {@link Currency}.
     * <p>
     * If the currency code is {@code null}, empty, or does not represent a valid
     * ISO 4217 currency code, this method logs a warning and returns the default
     * currency.
     *
     * @param currencyCode
     *            the ISO 4217 currency code to convert
     * @return the corresponding {@link Currency}, or the default currency when the
     *         code is {@code null}, empty, or invalid
     */
    public static Currency toCurrency(String currencyCode) {
        if (!StringUtils.hasLength(currencyCode)) {
            return DEFAULT;
        }

        try {
            return Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            log.warn("Unable to convert '{}' to Currency. Falling back to {}.", currencyCode,
                    DEFAULT.getCurrencyCode());

            return DEFAULT;
        }
    }

    /**
     * Converts the specified ISO 4217 currency code to a {@link Currency}.
     * <p>
     * If the currency code is {@code null}, empty, or does not represent a valid
     * ISO 4217 currency code, this method logs a warning and returns {@code null}.
     *
     * @param currencyCode
     *            the ISO 4217 currency code to convert
     * @return the corresponding {@link Currency}, or {@code null} when the code is
     *         {@code null}, empty, or invalid
     */
    public static Currency toOptionalCurrency(String currencyCode) {
        if (!StringUtils.hasLength(currencyCode)) {
            return null;
        }

        try {
            return Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            log.warn("Unable to convert '{}' to Currency. Returning null.", currencyCode);
            return null;
        }
    }

    /**
     * Determines whether the specified currency is the default currency.
     *
     * @param currency
     *            the currency to evaluate
     * @return {@code true} if the supplied currency is the default currency;
     *         {@code false} otherwise
     */
    public static boolean isDefaultCurrency(Currency currency) {
        return DEFAULT.equals(currency);
    }

    /**
     * Returns the default currency used by this utility class.
     *
     * @return the default {@link Currency} instance
     */
    public static Currency getDefaultCurrency() {
        return DEFAULT;
    }
}
