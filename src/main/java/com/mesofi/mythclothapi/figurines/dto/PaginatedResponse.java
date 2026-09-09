package com.mesofi.mythclothapi.figurines.dto;

import java.util.List;

/**
 * Represents a paginated response containing figurine data and pagination metadata.
 *
 * <p>This DTO is used to return paginated figurine search results while exposing additional
 * information about the number of collectable figurines available.
 *
 * <p>The response contains:
 *
 * <ul>
 *   <li>The figurines included in the current page.
 *   <li>The current page number.
 *   <li>The requested page size.
 *   <li>The total number of matching figurines.
 *   <li>The total number of collectable figurines.
 *   <li>The total number of available pages.
 * </ul>
 *
 * @param content list of figurines included in the current page
 * @param page current page index (zero-based)
 * @param size number of elements requested per page
 * @param totalElements total number of figurines matching the search criteria
 * @param totalCollectableElements total number of figurines considered collectable
 * @param totalPages total number of pages available based on the requested page size
 */
public record PaginatedResponse(
    List<FigurineResp> content,
    int page,
    int size,
    long totalElements,
    long totalCollectableElements,
    int totalPages) {}
