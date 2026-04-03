package com.mesofi.mythclothapi.figurines.mapper.converters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import com.opencsv.bean.AbstractBeanField;

class TrueFalseConverterTest {

  @Test
  void convert_shouldReturnTrue_whenValueIsTrueIgnoringCaseForRegularField() {
    // Arrange
    ExposedTrueFalseConverter converter = newConverterBoundTo("metalBody");

    // Act
    Boolean converted = converter.convertValue("TrUe");

    // Assert
    assertThat(converted).isTrue();
  }

  @Test
  void convert_shouldReturnFalse_whenValueIsFalseForRegularField() {
    // Arrange
    ExposedTrueFalseConverter converter = newConverterBoundTo("metalBody");

    // Act
    Boolean converted = converter.convertValue("FALSE");

    // Assert
    assertThat(converted).isFalse();
  }

  @Test
  void convert_shouldReturnFalse_whenValueIsNullForRegularField() {
    // Arrange
    ExposedTrueFalseConverter converter = newConverterBoundTo("metalBody");

    // Act
    Boolean converted = converter.convertValue(null);

    // Assert
    assertThat(converted).isFalse();
  }

  @Test
  void convert_shouldReturnFalse_whenValueIsNotTrueForRegularField() {
    // Arrange
    ExposedTrueFalseConverter converter = newConverterBoundTo("metalBody");

    // Act
    Boolean converted = converter.convertValue("yes");

    // Assert
    assertThat(converted).isFalse();
  }

  @Test
  void convert_shouldInvertResult_whenFieldIsArticulableAndValueIsTrue() {
    // Arrange
    ExposedTrueFalseConverter converter = newConverterBoundTo("articulable");

    // Act
    Boolean converted = converter.convertValue("TRUE");

    // Assert
    assertThat(converted).isFalse();
  }

  @Test
  void convert_shouldInvertResult_whenFieldIsArticulableAndValueIsNotTrue() {
    // Arrange
    ExposedTrueFalseConverter converter = newConverterBoundTo("articulable");

    // Act
    Boolean converted = converter.convertValue("");

    // Assert
    assertThat(converted).isTrue();
  }

  private ExposedTrueFalseConverter newConverterBoundTo(String fieldName) {
    try {
      ExposedTrueFalseConverter converter = new ExposedTrueFalseConverter();
      Field beanField = CsvBindingTarget.class.getDeclaredField(fieldName);
      Field abstractBeanFieldField = AbstractBeanField.class.getDeclaredField("field");
      abstractBeanFieldField.setAccessible(true);
      abstractBeanFieldField.set(converter, beanField);
      return converter;
    } catch (ReflectiveOperationException exception) {
      throw new RuntimeException(exception);
    }
  }

  private static final class ExposedTrueFalseConverter extends TrueFalseConverter {
    private Boolean convertValue(String value) {
      return super.convert(value);
    }
  }

  private static final class CsvBindingTarget {
    @SuppressWarnings("unused")
    private boolean articulable;

    @SuppressWarnings("unused")
    private boolean metalBody;
  }
}
