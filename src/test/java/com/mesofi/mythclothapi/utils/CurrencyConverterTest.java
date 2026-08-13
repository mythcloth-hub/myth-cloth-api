package com.mesofi.mythclothapi.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Currency;

import org.junit.jupiter.api.Test;

class CurrencyConverterTest {

    @Test
    void toCurrency_shouldReturnDefaultCurrency_whenCodeIsNull() {
        assertThat(CurrencyConverter.toCurrency(null)).isEqualTo(Currency.getInstance("JPY"));
    }

    @Test
    void toCurrency_shouldReturnDefaultCurrency_whenCodeIsBlank() {
        assertThat(CurrencyConverter.toCurrency("   ")).isEqualTo(Currency.getInstance("JPY"));
    }

    @Test
    void toCurrency_shouldReturnDefaultCurrency_whenCodeIsInvalid() {
        assertThat(CurrencyConverter.toCurrency("INVALID")).isEqualTo(Currency.getInstance("JPY"));
    }

    @Test
    void toCurrency_shouldReturnMatchingCurrency_whenCodeIsValid() {
        assertThat(CurrencyConverter.toCurrency("USD")).isEqualTo(Currency.getInstance("USD"));
    }

    @Test
    void isDefaultCurrency_shouldReturnTrueForDefaultCurrency() {
        assertThat(CurrencyConverter.isDefaultCurrency(Currency.getInstance("JPY"))).isTrue();
    }

    @Test
    void isDefaultCurrency_shouldReturnFalseForNonDefaultCurrency() {
        assertThat(CurrencyConverter.isDefaultCurrency(Currency.getInstance("USD"))).isFalse();
    }

    @Test
    void isDefaultCurrency_shouldReturnFalseForNull() {
        assertThat(CurrencyConverter.isDefaultCurrency(null)).isFalse();
    }
}
