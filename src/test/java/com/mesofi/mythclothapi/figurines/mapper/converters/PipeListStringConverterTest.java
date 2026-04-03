package com.mesofi.mythclothapi.figurines.mapper.converters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class PipeListStringConverterTest {

  private final PipeListStringConverter converter = new PipeListStringConverter();

  @Test
  void convert_shouldUsePipeDelimiter() {
    assertThat(converter.convert("alpha| beta|| gamma "))
        .isEqualTo(List.of("alpha", "beta", "gamma"));
  }
}
