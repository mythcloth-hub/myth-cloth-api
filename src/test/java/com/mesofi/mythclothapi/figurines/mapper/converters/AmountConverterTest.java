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
  void convert_shouldParseDigitsOnly_whenValueContainsSymbolsAndText() {
    assertThat(converter.convert(" USD 12,345.67 ")).isEqualTo(1234567D);
  }

  @Test
  void convert_shouldThrow_whenNoDigitsArePresent() {
    assertThatThrownBy(() -> converter.convert("USD")).isInstanceOf(NumberFormatException.class);
  }

  @Test
  void convert_shouldThrow_whenValueContainsOnlyWhitespaces() {
    assertThatThrownBy(() -> converter.convert("   ")).isInstanceOf(NumberFormatException.class);
  }
}
