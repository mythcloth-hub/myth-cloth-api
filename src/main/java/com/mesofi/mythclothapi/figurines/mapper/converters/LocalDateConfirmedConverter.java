package com.mesofi.mythclothapi.figurines.mapper.converters;

import static com.mesofi.mythclothapi.figurines.mapper.converters.LocalDateConverter.FULL;
import static com.mesofi.mythclothapi.figurines.mapper.converters.LocalDateConverter.YEAR_MONTH;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.springframework.util.StringUtils;

import com.mesofi.mythclothapi.figurines.mapper.LocalDateConfirmed;
import com.opencsv.bean.AbstractBeanField;

/**
 * Converts CSV date values into {@link LocalDateConfirmed} instances.
 *
 * <p>
 * The converter supports both complete dates and values containing only a month
 * and year. Complete dates are marked as confirmed, while month-year values are
 * assigned the first day of the month and marked as unconfirmed.
 * </p>
 *
 * <p>
 * Blank or unsupported values are converted to {@code null}.
 * </p>
 */
public class LocalDateConfirmedConverter extends AbstractBeanField<LocalDateConfirmed, String> {

    /**
     * Converts a CSV date value into a {@link LocalDateConfirmed}.
     *
     * <p>
     * Values matching the complete date format are parsed as a {@link LocalDate}
     * and marked as confirmed. Values containing only a month and year are parsed
     * as a {@link YearMonth}, assigned the first day of the month, and marked as
     * unconfirmed.
     *
     * @param value
     *            the CSV date value to convert
     * @return the converted date and confirmation status, or {@code null} when the
     *         value is blank or does not match a supported format
     */
    @Override
    protected LocalDateConfirmed convert(String value) {
        if (!StringUtils.hasLength(value)) {
            return null;
        }

        String text = value.trim();

        if (FULL.matcher(text).matches()) {
            return new LocalDateConfirmed(LocalDate.parse(text, DateTimeFormatter.ofPattern("M/d/yyyy")), true);
        }

        if (YEAR_MONTH.matcher(text).matches()) {
            YearMonth ym = YearMonth.parse(text, DateTimeFormatter.ofPattern("M/yyyy"));
            LocalDate localDate = ym.atDay(1);

            return new LocalDateConfirmed(localDate, false);
        }

        return null;
    }
}