package com.mesofi.mythclothapi.figurines.mapper.converters;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import com.opencsv.bean.AbstractBeanField;

public class LocalDateConverter extends AbstractBeanField<LocalDate, String> {

  public static final Pattern FULL = Pattern.compile("\\d{1,2}/\\d{1,2}/\\d{4}");
  public static final Pattern YEAR_MONTH = Pattern.compile("\\d{1,2}/\\d{4}");

  @Override
  protected LocalDate convert(String value) {
    if (!StringUtils.hasLength(value)) {
      return null;
    }

    String text = value.trim();
    if (FULL.matcher(text).matches()) {
      return LocalDate.parse(text, DateTimeFormatter.ofPattern("M/d/yyyy"));
    }
    if (YEAR_MONTH.matcher(text).matches()) {
      YearMonth ym = YearMonth.parse(text, DateTimeFormatter.ofPattern("M/yyyy"));
      return ym.atDay(1); // default day
    }

    return null;
  }
}
