package com.mesofi.mythclothapi.figurines;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.figurines.model.Figurine;

import lombok.NonNull;

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
   * Finds all figurines with advanced ordering and release status classification.
   *
   * <p>This query uses a derived table to compute a release status for each figurine based on the
   * presence and timing of distributor release/announcement dates. It joins the figurine with its
   * first distributor (by id) using a window function (ROW_NUMBER) to simulate the LATERAL join for
   * compatibility with H2 and other databases. The outer query orders results by release status and
   * relevant dates for consistent presentation.
   *
   * <pre>
   *   - RUMORED: No release or announcement date
   *   - PROTOTYPE: Announcement date exists, but no release date
   *   - ANNOUNCED: Release date exists and is in the future
   *   - RELEASED: Release date exists and is in the past or present
   * </pre>
   *
   * @param pageable pagination information
   * @return a page of all figurines
   */
  @Query(
      value =
          """
          SELECT
              is_articulable,
              is_broken,
              is_gold,
              is_golden,
              is_manga,
              is_metal_body,
              is_oce,
              is_plain_cloth,
              is_revival,
              is_set,
              is_surplice,
              anniversary_id,
              creation_date,
              distribution_id,
              group_id,
              id,
              lineup_id,
              series_id,
              update_date,
              tamashii_url,
              normalized_name,
              legacy_name,
              remarks
          FROM (
              SELECT
                  CASE
                      WHEN fd.release_date IS NULL AND fd.announcement_date IS NULL THEN 'RUMORED'
                      WHEN fd.release_date IS NULL AND fd.announcement_date IS NOT NULL THEN 'PROTOTYPE'
                      WHEN fd.release_date IS NOT NULL AND fd.release_date > CURRENT_DATE THEN 'ANNOUNCED'
                      ELSE 'RELEASED'
                  END AS release_status,
                  fd.announcement_date,
                  fd.release_date,
                  f.*
              FROM figurines f
              LEFT JOIN (
                  SELECT *
                  FROM (
                      SELECT
                          fd.*,
                          ROW_NUMBER() OVER (
                              PARTITION BY fd.figurine_id
                              ORDER BY fd.id
                          ) AS rn
                      FROM figurine_distributor fd
                  ) ranked
                  WHERE rn = 1
              ) fd ON fd.figurine_id = f.id
          ) t
          ORDER BY
              CASE release_status
                  WHEN 'ANNOUNCED' THEN 1
                  WHEN 'RELEASED'  THEN 2
                  WHEN 'PROTOTYPE' THEN 3
                  WHEN 'RUMORED'   THEN 4
              END,
              CASE
                  WHEN release_status IN ('ANNOUNCED', 'RELEASED') THEN release_date
              END DESC,
              CASE
                  WHEN release_status = 'PROTOTYPE' THEN announcement_date
              END DESC,
              CASE
                  WHEN release_status = 'RUMORED' THEN creation_date
              END
          """,
      countQuery = """
          SELECT COUNT(*)
          FROM figurines f;
          """,
      nativeQuery = true)
  @NonNull
  Page<Figurine> findAll(@NonNull Pageable pageable);

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
