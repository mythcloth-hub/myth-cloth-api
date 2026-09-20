package com.mesofi.mythclothapi.support;

import java.util.List;

/**
 * A generic class to represent a paginated response.
 *
 * @param <T>
 *            the type of the content in the page
 */
public record PageResponse<T>(List<T> content, PageMetadata page) {

    /**
     * A record to hold metadata about the page.
     *
     * @param size
     *            the size of the page
     * @param number
     *            the current page number
     * @param totalElements
     *            the total number of elements across all pages
     * @param totalPages
     *            the total number of pages
     */
    public record PageMetadata(int size, int number, long totalElements, int totalPages) {
    }
}
