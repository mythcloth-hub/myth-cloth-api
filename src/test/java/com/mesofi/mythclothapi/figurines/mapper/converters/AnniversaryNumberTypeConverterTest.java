package com.mesofi.mythclothapi.figurines.mapper.converters;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.mesofi.mythclothapi.anniversaries.model.AnniversaryType;
import com.mesofi.mythclothapi.figurines.mapper.AnniversaryNumberType;

class AnniversaryNumberTypeConverterTest {

  private final ExposedAnniversaryNumberTypeConverter converter =
      new ExposedAnniversaryNumberTypeConverter();

  @Test
  void convert_shouldReturnNull_whenValueIsNull() {
    assertThat(converter.convertValue(null)).isNull();
  }

  @Test
  void convert_shouldReturnNull_whenValueIsBlank() {
    assertThat(converter.convertValue("   ")).isNull();
  }

  @Test
  void convert_shouldParseYearOnly_whenTypeIsMissing() {
    AnniversaryNumberType converted = converter.convertValue("40");

    assertThat(converted).isNotNull();
    assertThat(converted.getAnniversaryNumber()).isEqualTo(40);
    assertThat(converted.getAnniversaryType()).isNull();
  }

  @Test
  void convert_shouldParseYearAndType_whenTypeIsProvidedWithLowercaseAndWhitespace() {
    AnniversaryNumberType converted = converter.convertValue("20| saint_seiya ");

    assertThat(converted).isNotNull();
    assertThat(converted.getAnniversaryNumber()).isEqualTo(20);
    assertThat(converted.getAnniversaryType()).isEqualTo(AnniversaryType.SAINT_SEIYA);
  }

  @Test
  void convert_shouldParseYearAndType_whenTypeIsProvidedInAnotherEnumValue() {
    AnniversaryNumberType converted = converter.convertValue("30|tamashii_nations_world_tour");

    assertThat(converted).isNotNull();
    assertThat(converted.getAnniversaryNumber()).isEqualTo(30);
    assertThat(converted.getAnniversaryType())
        .isEqualTo(AnniversaryType.TAMASHII_NATIONS_WORLD_TOUR);
  }

  private static final class ExposedAnniversaryNumberTypeConverter
      extends AnniversaryNumberTypeConverter {
    private AnniversaryNumberType convertValue(String value) {
      return super.convert(value);
    }
  }
}
