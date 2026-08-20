package com.mesofi.mythclothapi.figurines.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.figurines.model.Figurine;

/**
 * Defines query operations for retrieving figurine data.
 *
 * <p>
 * This repository provides query operations that require custom filtering,
 * sorting, pagination, or aggregation logic beyond standard Spring Data
 * repository capabilities.
 * </p>
 *
 * <p>
 * Supported operations include:
 * </p>
 * <ul>
 * <li>Paginated figurine searches with collectable figurine metadata.</li>
 * <li>Retrieval of figurines matching dynamic filter criteria.</li>
 * <li>Retrieval of figurines released during a specific year.</li>
 * </ul>
 *
 * @see FigurineRepositoryImpl
 * @see FigurineFilter
 */
public interface FigurineQueryRepository {

    /**
     * Retrieves a paginated list of figurines matching the specified filter
     * criteria.
     *
     * <p>
     * The returned page includes the matching figurines, pagination metadata, the
     * total number of matching figurines, and the total number of collectable
     * figurines.
     * </p>
     *
     * @param filter
     *            filtering criteria used to restrict the figurine search; may be
     *            {@code null}
     * @param pageable
     *            pagination configuration, including page size and offset
     * @return a paginated result containing the matching figurines and collectable
     *         figurine count
     */
    CollectablePageImpl<Figurine> findPaginated(FigurineFilter filter, Pageable pageable);

    /**
     * Retrieves all figurines matching the specified filter criteria.
     *
     * <p>
     * Results are returned according to the ordering defined by the repository
     * implementation.
     * </p>
     *
     * @param filter
     *            filtering criteria used to restrict the figurine search; may be
     *            {@code null}
     * @return a list of figurines matching the filter criteria
     */
    List<Figurine> findAll(FigurineFilter filter);

    /**
     * Retrieves all figurines whose release date falls within the specified year.
     *
     * @param year
     *            year used to filter figurines by release date
     * @return a list of figurines released during the specified year
     */
    List<Figurine> findAllByYear(int year);
}
