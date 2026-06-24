package com.mesofi.mythclothapi.figurines.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.figurines.model.Figurine;

/**
 * Defines custom repository operations for retrieving figurine data.
 *
 * <p>This interface provides query operations that extend the default Spring Data repository
 * capabilities. Implementations may use custom query strategies to support advanced filtering,
 * sorting, and pagination requirements.
 *
 * <p>The available operations support:
 *
 * <ul>
 *   <li>Paginated figurine searches with additional collectable metadata.
 *   <li>Filtering figurines using {@link FigurineFilter}.
 *   <li>Retrieving figurines released within a specific year.
 * </ul>
 *
 * @see FigurineRepositoryImpl
 * @see FigurineFilter
 */
public interface FigurineRepositoryCustom {
  /**
   * Retrieves a paginated list of figurines matching the provided filter criteria.
   *
   * <p>The returned page includes standard pagination information plus the total number of
   * collectable figurines.
   *
   * @param filter filtering criteria used to restrict the figurine search
   * @param pageable pagination information including page number, size, and sorting
   * @return a paginated result containing figurines and collectable count information
   */
  CollectablePageImpl<Figurine> findPaginated(FigurineFilter filter, Pageable pageable);

  /**
   * Retrieves a list of figurines matching the provided filter criteria.
   *
   * @param filter filtering criteria used to restrict the figurine search
   * @return a list of figurines matching the filter criteria
   */
  List<Figurine> findAll(FigurineFilter filter);

  /**
   * Retrieves a list of figurines released within the specified year.
   *
   * @param year the year to filter figurines by release date
   * @return a list of figurines released in the specified year
   */
  List<Figurine> findAllByYear(int year);
}
