package com.mesofi.mythclothapi.figurines.mapper.converters;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.opencsv.bean.AbstractBeanField;

public class ListStringConverter extends AbstractBeanField<List<String>, String> {
  @Override
  protected List<String> convert(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }
}
