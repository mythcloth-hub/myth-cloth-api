package com.mesofi.mythclothapi.figurines.mapper.converters;

import com.mesofi.mythclothapi.anniversaries.model.AnniversaryType;
import com.mesofi.mythclothapi.figurines.mapper.AnniversaryNumberType;
import com.opencsv.bean.AbstractBeanField;

/**
 * Converts a CSV value containing an anniversary year and optional anniversary
 * type into an {@link AnniversaryNumberType}.
 *
 * <p>
 * The expected format is {@code year|type}, where the anniversary type is
 * optional. For example, {@code 20|REGULAR} represents a 20th regular
 * anniversary, while {@code 20} represents an anniversary without a specified
 * type.
 * </p>
 *
 * <p>
 * The anniversary type is matched against {@link AnniversaryType} values
 * case-insensitively.
 * </p>
 */
public class AnniversaryNumberTypeConverter extends AbstractBeanField<AnniversaryNumberType, String> {

    /**
     * Converts a CSV anniversary value into an {@link AnniversaryNumberType}.
     *
     * @param value
     *            the CSV value containing the anniversary year and optional type
     * @return the converted anniversary information, or {@code null} when the input
     *         is blank
     * @throws NumberFormatException
     *             if the anniversary year is not a valid integer
     * @throws IllegalArgumentException
     *             if the specified anniversary type does not match an
     *             {@link AnniversaryType} value
     */
    @Override
    protected AnniversaryNumberType convert(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String[] yearType = value.strip().split("\\|");
        int year = Integer.parseInt(yearType[0]);

        if (yearType.length == 1) {
            return new AnniversaryNumberType(null, year);
        }

        AnniversaryType anniversaryType = AnniversaryType.valueOf(yearType[1].trim().toUpperCase());
        return new AnniversaryNumberType(anniversaryType, year);
    }
}