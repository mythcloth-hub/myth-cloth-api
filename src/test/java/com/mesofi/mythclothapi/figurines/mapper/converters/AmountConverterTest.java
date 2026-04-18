package com.mesofi.mythclothapi.figurines.mapper.converters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AmountConverterTest {

  private final AmountConverter converter = new AmountConverter();

  @Test
  void convert_shouldReturnNull_whenValueIsNull() {
    assertThat(converter.convert(null)).isNull();
  }

  @Test
  void convert_shouldReturnNull_whenValueIsEmpty() {
    assertThat(converter.convert("")).isNull();
  }

  @Test
  void convert_shouldKeepOnlyDigits_whenValueContainsSymbolsAndSeparators() {
    assertThat(converter.convert("USD 1,234.99")).isEqualTo(123499D);
  }

  @Test
  void convert_shouldParsePlainNumericValue() {
    assertThat(converter.convert("250")).isEqualTo(250D);
  }

  @Test
  void convert_shouldThrowNumberFormatException_whenValueHasNoDigits() {
    assertThatThrownBy(() -> converter.convert("USD")).isInstanceOf(NumberFormatException.class);
  }
}
