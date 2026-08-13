package com.mesofi.mythclothapi.figurines.mapper.converters;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import com.opencsv.bean.AbstractBeanField;

/**
 * Converts date values from their CSV string representation into
 * {@link LocalDate} instances.
 *
 * <p>
 * The converter supports the following date formats:
 * </p>
 *
 * <ul>
 * <li>{@code M/d/yyyy}, for example {@code 8/12/2026}</li>
 * <li>{@code M/yyyy}, for example {@code 8/2026}</li>
 * </ul>
 *
 * <p>
 * When only a year and month are provided, the first day of the month is used
 * as the default day.
 * </p>
 *
 * <p>
 * Blank or unsupported values are converted to {@code null}.
 * </p>
 */
public class LocalDateConverter extends AbstractBeanField<LocalDate, String> {

    /**
     * Pattern used to identify values containing a complete date.
     */
    public static final Pattern FULL = Pattern.compile("\\d{1,2}/\\d{1,2}/\\d{4}");

    /**
     * Pattern used to identify values containing only a month and year.
     */
    public static final Pattern YEAR_MONTH = Pattern.compile("\\d{1,2}/\\d{4}");

    /**
     * Converts a CSV date value into a {@link LocalDate}.
     *
     * <p>
     * Full dates are parsed using the {@code M/d/yyyy} format. Values containing
     * only a month and year are parsed as {@link YearMonth} and assigned the first
     * day of the month.
     *
     * @param value
     *            the CSV date value to convert
     * @return the parsed date, or {@code null} when the value is blank or does not
     *         match a supported format
     */
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
            return ym.atDay(1);
        }

        return null;
    }
}