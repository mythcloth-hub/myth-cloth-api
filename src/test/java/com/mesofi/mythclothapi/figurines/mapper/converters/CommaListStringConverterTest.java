package com.mesofi.mythclothapi.figurines.mapper.converters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class CommaListStringConverterTest {

  private final CommaListStringConverter converter = new CommaListStringConverter();

  @Test
  void getDelimiter_shouldReturnComma() {
    assertThat(converter.getDelimiter()).isEqualTo(",");
  }

  @Test
  void convert_shouldUseCommaDelimiter() {
    assertThat(converter.convert("alpha, beta,, gamma "))
        .isEqualTo(List.of("alpha", "beta", "gamma"));
  }
}
