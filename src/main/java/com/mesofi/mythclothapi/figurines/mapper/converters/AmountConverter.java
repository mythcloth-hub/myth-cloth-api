package com.mesofi.mythclothapi.figurines.mapper.converters;

import java.util.Optional;

import org.springframework.util.StringUtils;

import com.opencsv.bean.AbstractBeanField;

/**
 * Converts CSV amount values into {@link Double} values.
 *
 * <p>
 * The converter removes all non-numeric characters from the input before
 * parsing the resulting value. This allows values containing currency symbols,
 * separators, or other formatting characters to be converted into numeric
 * amounts.
 * </p>
 *
 * <p>
 * Blank or {@code null} values are converted to {@code null}.
 * </p>
 */
public class AmountConverter extends AbstractBeanField<Double, String> {

    /**
     * Converts a formatted CSV amount into a numeric value.
     *
     * <p>
     * All characters other than digits are removed before the value is parsed.
     * </p>
     *
     * @param value
     *            the CSV amount value to convert
     * @return the parsed amount, or {@code null} when the input is {@code null} or
     *         blank
     * @throws NumberFormatException
     *             if the input contains no valid numeric value after formatting
     */
    @Override
    protected Double convert(String value) {
        return Optional.ofNullable(value).filter(StringUtils::hasLength)
                .map(amountString -> amountString.replaceAll("[^0-9]", "")).map(Double::parseDouble).orElse(null);
    }
}