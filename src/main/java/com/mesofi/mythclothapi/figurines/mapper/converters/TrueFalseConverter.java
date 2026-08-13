package com.mesofi.mythclothapi.figurines.mapper.converters;

import java.util.Optional;

import com.opencsv.bean.AbstractBeanField;

/**
 * Converts CSV string values representing boolean flags into {@link Boolean}
 * values.
 *
 * <p>
 * The converter interprets the value {@code "TRUE"} (case-insensitive) as
 * {@code true}. Null values and any other values are interpreted as
 * {@code false}.
 *
 * <p>
 * For the {@code articulable} field, the resulting value is inverted to
 * accommodate the semantics of the source CSV data.
 */
public class TrueFalseConverter extends AbstractBeanField<Boolean, String> {

    /**
     * Converts a CSV value into a boolean value.
     *
     * <p>
     * The value {@code "TRUE"} is converted to {@code true}; all other values,
     * including {@code null}, are converted to {@code false}. The result is
     * inverted when the converter is bound to the {@code articulable} field.
     *
     * @param value
     *            the CSV value to convert
     * @return the converted boolean value
     */
    @Override
    protected Boolean convert(String value) {
        boolean result = Optional.ofNullable(value).map("TRUE"::equalsIgnoreCase).orElse(false);

        // Name of the Java field this annotation is bound to
        String fieldName = this.getField().getName();

        // Negate only for the articulable field
        if ("articulable".equals(fieldName)) {
            return !result;
        }

        return result;
    }
}