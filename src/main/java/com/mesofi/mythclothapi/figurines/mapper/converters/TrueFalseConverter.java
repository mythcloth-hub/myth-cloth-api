package com.mesofi.mythclothapi.figurines.mapper.converters;

import java.util.Optional;

import com.opencsv.bean.AbstractBeanField;

public class TrueFalseConverter extends AbstractBeanField<Boolean, String> {
  @Override
  protected Boolean convert(String value) {
    boolean result = Optional.ofNullable(value).map("TRUE"::equalsIgnoreCase).orElse(false);

    // Name of the Java field this annotation is bound to
    String fieldName = this.getField().getName();

    // Negate only for specific field
    if ("articulable".equals(fieldName)) {
      return !result;
    }
    return result;
  }
}
