package com.mesofi.mythclothapi.figurines.mapper.converters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class ListStringConverterTest {

  private final ListStringConverter converter = new SemicolonListStringConverter();

  @Test
  void convert_shouldReturnNull_whenValueIsNull() {
    assertThat(converter.convert(null)).isNull();
  }

  @Test
  void convert_shouldReturnNull_whenValueIsBlank() {
    assertThat(converter.convert("   ")).isNull();
  }

  @Test
  void convert_shouldSplitTrimAndDiscardEmptyTokens() {
    assertThat(converter.convert(" alpha ; ; beta ;  gamma  ; "))
        .isEqualTo(List.of("alpha", "beta", "gamma"));
  }

  @Test
  void convert_shouldReturnEmptyList_whenValueContainsOnlyDelimiters() {
    assertThat(converter.convert(" ; ; ; ")).isEmpty();
  }

  private static final class SemicolonListStringConverter extends ListStringConverter {
    @Override
    String getDelimiter() {
      return ";";
    }
  }
}
