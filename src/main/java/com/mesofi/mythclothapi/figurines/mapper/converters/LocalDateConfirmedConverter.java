package com.mesofi.mythclothapi.figurines.mapper.converters;

import static com.mesofi.mythclothapi.figurines.mapper.converters.LocalDateConverter.FULL;
import static com.mesofi.mythclothapi.figurines.mapper.converters.LocalDateConverter.YEAR_MONTH;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.springframework.util.StringUtils;

import com.mesofi.mythclothapi.figurines.mapper.LocalDateConfirmed;
import com.opencsv.bean.AbstractBeanField;

public class LocalDateConfirmedConverter extends AbstractBeanField<LocalDateConfirmed, String> {

  @Override
  protected LocalDateConfirmed convert(String value) {
    if (!StringUtils.hasLength(value)) {
      return null;
    }

    String text = value.trim();
    if (FULL.matcher(text).matches()) {
      return new LocalDateConfirmed(
          LocalDate.parse(text, DateTimeFormatter.ofPattern("M/d/yyyy")), true);
    }
    if (YEAR_MONTH.matcher(text).matches()) {
      YearMonth ym = YearMonth.parse(text, DateTimeFormatter.ofPattern("M/yyyy"));
      LocalDate localDate = ym.atDay(1); // default day
      return new LocalDateConfirmed(localDate, false);
    }
    return null;
  }
}
