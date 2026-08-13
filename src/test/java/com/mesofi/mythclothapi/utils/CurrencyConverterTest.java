package com.mesofi.mythclothapi.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Currency;

import org.junit.jupiter.api.Test;

class CurrencyConverterTest {

    @Test
    void toCurrency_shouldReturnDefaultCurrencyWhenInputIsNullEmptyOrInvalid() {
        assertThat(CurrencyConverter.toCurrency(null)).isEqualTo(Currency.getInstance("JPY"));
        assertThat(CurrencyConverter.toCurrency("")).isEqualTo(Currency.getInstance("JPY"));
        assertThat(CurrencyConverter.toCurrency("invalid")).isEqualTo(Currency.getInstance("JPY"));
    }

    @Test
    void toCurrency_shouldReturnCurrencyWhenInputIsValid() {
        assertThat(CurrencyConverter.toCurrency("USD")).isEqualTo(Currency.getInstance("USD"));
    }

    @Test
    void isDefaultCurrency_shouldReturnTrueOnlyForDefaultCurrency() {
        assertThat(CurrencyConverter.isDefaultCurrency(Currency.getInstance("JPY"))).isTrue();
        assertThat(CurrencyConverter.isDefaultCurrency(Currency.getInstance("USD"))).isFalse();
    }
}
