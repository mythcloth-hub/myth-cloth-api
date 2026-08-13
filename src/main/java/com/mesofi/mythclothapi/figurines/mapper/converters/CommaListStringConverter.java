package com.mesofi.mythclothapi.figurines.mapper.converters;

/**
 * Converts comma-delimited CSV values into lists of strings.
 *
 * <p>
 * This converter specializes {@link ListStringConverter} by using a comma
 * ({@code ,}) as the delimiter between individual values.
 * </p>
 */
public class CommaListStringConverter extends ListStringConverter {

    /**
     * Returns the delimiter used to separate values in the source data.
     *
     * @return the comma delimiter
     */
    @Override
    String getDelimiter() {
        return ",";
    }
}