package com.mesofi.mythclothapi.figurines.mapper.converters;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.opencsv.bean.AbstractBeanField;

/**
 * Base converter for transforming delimited CSV values into lists of strings.
 *
 * <p>
 * Subclasses define the delimiter used to separate individual values through
 * {@link #getDelimiter()}.
 * </p>
 *
 * <p>
 * Blank or {@code null} input values are converted to {@code null}. Individual
 * values are trimmed and empty elements are excluded from the resulting list.
 * </p>
 */
public abstract class ListStringConverter extends AbstractBeanField<List<String>, String> {

    /**
     * Converts a delimited string into a list of non-empty, trimmed strings.
     *
     * @param value
     *            the delimited CSV value to convert
     * @return a list of parsed values, or {@code null} when the input is
     *         {@code null} or blank
     */
    @Override
    protected List<String> convert(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return Arrays.stream(value.split(getDelimiter())).map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Returns the delimiter used to separate values in the source data.
     *
     * <p>
     * The returned value is used as the regular expression passed to
     * {@link String#split(String)}.
     * </p>
     *
     * @return the regular expression representing the delimiter
     */
    abstract String getDelimiter();
}