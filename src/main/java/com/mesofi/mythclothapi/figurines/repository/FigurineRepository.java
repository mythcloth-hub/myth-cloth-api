package com.mesofi.mythclothapi.figurines.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.figurines.model.Figurine;

/**
 * Repository interface for {@link Figurine} persistence and query operations.
 *
 * <p>Provides advanced search and pagination capabilities, including:
 *
 * <ul>
 *   <li>Custom ordering and release status logic for all figurines
 *   <li>Case-insensitive substring search on normalized names
 *   <li>Standard CRUD operations via {@link JpaRepository}
 * </ul>
 *
 * <p>The custom queries leverage SQL logic to classify figurines by release status:
 *
 * <ul>
 *   <li><b>RUMORED</b>: No release or announcement date
 *   <li><b>PROTOTYPE</b>: Announcement date exists, but no release date
 *   <li><b>ANNOUNCED</b>: Release date exists and is in the future
 *   <li><b>RELEASED</b>: Release date exists and is in the past or present
 * </ul>
 *
 * Results are ordered by release status and relevant dates for consistent presentation.
 */
@Repository
public interface FigurineRepository
    extends JpaRepository<Figurine, Long>, FigurineRepositoryCustom {

  /**
   * Finds a figurine by its legacy name.
   *
   * <p>This method returns an {@link Optional} containing the figurine whose legacy name matches
   * the provided value. If no figurine exists with the given legacy name, an empty {@link Optional}
   * is returned. The legacy name is expected to be unique, but may be {@code null} for some
   * figurines.
   *
   * @param legacyName the legacy name of the figurine to search for (maybe {@code null})
   * @return an {@link Optional} containing the matching figurine, or empty if no match exists
   */
  Optional<Figurine> findByLegacyName(String legacyName);
}
