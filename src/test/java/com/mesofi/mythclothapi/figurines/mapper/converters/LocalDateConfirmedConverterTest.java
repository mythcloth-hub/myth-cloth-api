package com.mesofi.mythclothapi.figurines.mapper.converters;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.mesofi.mythclothapi.figurines.mapper.LocalDateConfirmed;

class LocalDateConfirmedConverterTest {

  private final LocalDateConfirmedConverter converter = new LocalDateConfirmedConverter();

  @Test
  void convert_shouldReturnNull_whenValueIsNull() {
    assertThat(converter.convert(null)).isNull();
  }

  @Test
  void convert_shouldReturnNull_whenValueIsBlank() {
    assertThat(converter.convert("   ")).isNull();
  }

  @Test
  void convert_shouldParseFullDateAndMarkAsConfirmed() {
    LocalDateConfirmed converted = converter.convert("3/5/2025");

    assertThat(converted).isNotNull();
    assertThat(converted.getDate()).isEqualTo(LocalDate.of(2025, 3, 5));
    assertThat(converted.isConfirmed()).isTrue();
  }

  @Test
  void convert_shouldParseYearMonthWithDefaultDayAndMarkAsNotConfirmed() {
    LocalDateConfirmed converted = converter.convert("3/2025");

    assertThat(converted).isNotNull();
    assertThat(converted.getDate()).isEqualTo(LocalDate.of(2025, 3, 1));
    assertThat(converted.isConfirmed()).isFalse();
  }

  @Test
  void convert_shouldTrimValueBeforeParsing() {
    LocalDateConfirmed converted = converter.convert(" 3/2025 ");

    assertThat(converted).isNotNull();
    assertThat(converted.getDate()).isEqualTo(LocalDate.of(2025, 3, 1));
    assertThat(converted.isConfirmed()).isFalse();
  }

  @Test
  void convert_shouldReturnNull_whenPatternDoesNotMatch() {
    assertThat(converter.convert("2025-03-05")).isNull();
  }
}
