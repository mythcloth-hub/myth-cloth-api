package com.mesofi.mythclothapi.figurines;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.figurines.model.Figurine;

import lombok.NonNull;

/** Repository for {@link Figurine} persistence and read operations. */
@Repository
public interface FigurineRepository extends JpaRepository<Figurine, Long> {

  /**
   * Finds figurines whose normalized name contains the given string (case-insensitive).
   *
   * @param name name filter
   * @param pageable pagination and page-size information
   * @return a page of figurines matching the name filter
   */
  Page<Figurine> findByNormalizedNameContainingIgnoreCase(String name, Pageable pageable);

  /**
   * Returns figurines using JP catalog ordering rules.
   *
   * <p>SQL equivalent of the HQL query declared in {@link Query}:
   *
   * <pre>{@code
   * SELECT
   *     t.release_status,
   *     t.release_date,
   *     t.announcement_date,
   *     t.id,
   *     t.legacy_name,
   *     t.creation_date
   * FROM (
   *     SELECT
   *         CASE
   *             WHEN fd.release_date IS NULL
   *                  AND fd.announcement_date IS NULL THEN 'RUMORED'
   *             WHEN fd.release_date IS NULL
   *                  AND fd.announcement_date IS NOT NULL THEN 'PROTOTYPE'
   *             WHEN fd.release_date IS NOT NULL
   *                  AND fd.release_date > CURRENT_DATE THEN 'ANNOUNCED'
   *             ELSE 'RELEASED'
   *         END AS release_status,
   *         fd.release_date,
   *         fd.announcement_date,
   *         f.id,
   *         f.legacy_name,
   *         f.creation_date
   *     FROM
   *         figurines f
   *     LEFT JOIN (
   *         SELECT
   *             fd.id,
   *             fd.figurine_id,
   *             fd.distributor_id,
   *             fd.announcement_date,
   *             fd.release_date
   *         FROM
   *             figurine_distributor fd
   *         JOIN distributors d
   *             ON fd.distributor_id = d.id
   *         WHERE
   *             d.country = 'JP'
   *     ) fd
   *         ON fd.figurine_id = f.id
   *     LEFT JOIN distributors d
   *         ON fd.distributor_id = d.id
   * ) t
   * ORDER BY
   *     CASE t.release_status
   *         WHEN 'ANNOUNCED' THEN 1
   *         WHEN 'RELEASED'  THEN 2
   *         WHEN 'PROTOTYPE' THEN 3
   *         WHEN 'RUMORED'   THEN 4
   *     END,
   *
   *     CASE
   *         WHEN release_status IN ('ANNOUNCED', 'RELEASED')
   *             THEN release_date
   *     END DESC,
   *
   *     CASE
   *         WHEN release_status = 'PROTOTYPE'
   *             THEN announcement_date
   *     END DESC,
   *
   *     CASE
   *         WHEN release_status = 'RUMORED'
   *             THEN creation_date
   *     END;
   * }</pre>
   *
   * @param pageable pagination and page-size information
   * @return a page of figurines following catalog ordering rules
   */
  @Query(
      value =
          """
          select f
          from Figurine f
          left join f.distributors fd
            on fd.distributor.country = com.mesofi.mythclothapi.distributors.model.CountryCode.JP
          order by
            case
              when fd.releaseDate is null and fd.announcementDate is null then 4
              when fd.releaseDate is null and fd.announcementDate is not null then 3
              when fd.releaseDate is not null and fd.releaseDate > current_date then 1
              else 2
            end,
            case
              when fd.releaseDate is not null
              then fd.releaseDate
            end desc,
            case
              when fd.announcementDate is not null
              then fd.announcementDate
            end desc,
            case
              when fd.releaseDate is null and fd.announcementDate is null
              then f.creationDate
            end
          """,
      countQuery = """
          select count(f)
          from Figurine f
          """)
  Page<Figurine> findAll(@NonNull Pageable pageable);

  /**
   * Finds a figurine by legacy name.
   *
   * @param legacyName figurine legacy name
   * @return matching figurine, or empty if no match exists
   */
  Optional<Figurine> findByLegacyName(String legacyName);
}
