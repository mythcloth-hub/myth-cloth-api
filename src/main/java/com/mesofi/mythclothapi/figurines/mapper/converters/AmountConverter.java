package com.mesofi.mythclothapi.figurines.mapper.converters;

import java.util.Optional;

import org.springframework.util.StringUtils;

import com.opencsv.bean.AbstractBeanField;

public class AmountConverter extends AbstractBeanField<Double, String> {
  @Override
  protected Double convert(String value) {

    return Optional.ofNullable(value)
        .filter(StringUtils::hasLength)
        .map(amountString -> amountString.replaceAll("[^0-9]", ""))
        .map(Double::parseDouble)
        .orElse(null);
  }
}
