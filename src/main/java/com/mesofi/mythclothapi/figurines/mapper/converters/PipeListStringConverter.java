package com.mesofi.mythclothapi.figurines.mapper.converters;

/**
 * Converts pipe-delimited string values into lists of strings.
 *
 * <p>
 * This converter specializes {@link ListStringConverter} by using the pipe
 * character ({@code |}) as the delimiter between list elements.
 * </p>
 */
public class PipeListStringConverter extends ListStringConverter {

    /**
     * Returns the regular expression used to split pipe-delimited values.
     *
     * @return the escaped pipe delimiter
     */
    @Override
    String getDelimiter() {
        return "\\|";
    }
}