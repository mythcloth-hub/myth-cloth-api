package com.mesofi.mythclothapi.figurines.mapper.converters;

import com.mesofi.mythclothapi.anniversaries.model.AnniversaryType;
import com.mesofi.mythclothapi.figurines.mapper.AnniversaryNumberType;
import com.opencsv.bean.AbstractBeanField;

public class AnniversaryNumberTypeConverter
    extends AbstractBeanField<AnniversaryNumberType, String> {

  @Override
  protected AnniversaryNumberType convert(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }

    String[] yearType = value.strip().split("\\|");
    int year = Integer.parseInt(yearType[0]);

    if (yearType.length == 1) {
      return new AnniversaryNumberType(null, year);
    } else {
      AnniversaryType anniversaryType = AnniversaryType.valueOf(yearType[1].trim().toUpperCase());
      return new AnniversaryNumberType(anniversaryType, year);
    }
  }
}
