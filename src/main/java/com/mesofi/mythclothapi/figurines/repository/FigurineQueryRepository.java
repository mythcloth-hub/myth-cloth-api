package com.mesofi.mythclothapi.figurines.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.model.FigurineWithCollectionId;
import com.mesofi.mythclothapi.figurines.repository.projection.FigurineSearchProjection;

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

    CollectablePageImpl<FigurineSearchProjection> findAll(FigurineFilter filter, Pageable pageable, Long collectionId);

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
     * @see #findPaginated(FigurineFilter, Pageable, Long)
     */
    CollectablePageImpl<Figurine> findPaginated(FigurineFilter filter, Pageable pageable);

    /**
     * Retrieves a paginated list of figurines matching the specified filter
     * criteria and belonging to the specified collection. Even though this method
     * is similar to {@link #findPaginated(FigurineFilter, Pageable)}, it also
     * filters the results by collection ID.
     *
     * @param filter
     *            filtering criteria used to restrict the figurine search; may be
     *            {@code null}
     * @param pageable
     *            pagination configuration, including page size and offset
     * @param collectionId
     *            identifier of the collection to which the figurines must belong
     * @return a paginated result containing the matching figurines and collectable
     *         figurine count
     */
    CollectablePageImpl<FigurineWithCollectionId> findPaginated(FigurineFilter filter, Pageable pageable,
            Long collectionId);

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
