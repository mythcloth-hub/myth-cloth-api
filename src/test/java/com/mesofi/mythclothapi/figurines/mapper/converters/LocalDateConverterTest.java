package com.mesofi.mythclothapi.figurines.mapper.converters;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class LocalDateConverterTest {

  private final LocalDateConverter converter = new LocalDateConverter();

  @Test
  void convert_shouldReturnNull_whenValueIsNull() {
    assertThat(converter.convert(null)).isNull();
  }

  @Test
  void convert_shouldReturnNull_whenValueIsBlank() {
    assertThat(converter.convert("   ")).isNull();
  }

  @Test
  void convert_shouldParseFullDatePattern() {
    assertThat(converter.convert("3/5/2025")).isEqualTo(LocalDate.of(2025, 3, 5));
  }

  @Test
  void convert_shouldParseTrimmedFullDatePattern() {
    assertThat(converter.convert(" 3/5/2025 ")).isEqualTo(LocalDate.of(2025, 3, 5));
  }

  @Test
  void convert_shouldParseYearMonthPatternWithDefaultDayOne() {
    assertThat(converter.convert("3/2025")).isEqualTo(LocalDate.of(2025, 3, 1));
  }

  @Test
  void convert_shouldTrimValueBeforeParsing() {
    assertThat(converter.convert(" 3/2025 ")).isEqualTo(LocalDate.of(2025, 3, 1));
  }

  @Test
  void convert_shouldReturnNull_whenPatternDoesNotMatch() {
    assertThat(converter.convert("2025-03-05")).isNull();
  }

  @Test
  void convert_shouldReturnNull_whenYearMonthIsIncomplete() {
    assertThat(converter.convert("3/25")).isNull();
  }
}
