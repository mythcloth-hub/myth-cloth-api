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
   * Returns figurines for distributor {@code 1} using catalog ordering rules.
   *
   * <p>SQL equivalent of the HQL query declared in {@link Query}:
   *
   * <pre>{@code
   * SELECT
   *     *
   * FROM (
   *     SELECT
   *         CASE
   *             WHEN CURRENT_DATE > fd.release_date
   *                  AND fd.distributor_id IS NOT NULL
   *                 THEN 'RELEASED'
   *
   *             WHEN fd.release_date >= CURRENT_DATE
   *                  AND fd.distributor_id IS NOT NULL
   *                 THEN 'ANNOUNCED'
   *
   *             WHEN fd.announcement_date IS NOT NULL
   *                 THEN 'PROTOTYPE'
   *
   *             ELSE 'RUMORED'
   *         END AS release_status,
   *         fd.release_date,
   *         fd.announcement_date,
   *         f. *
   *     FROM
   *         figurines f
   *         JOIN figurine_distributor fd
   *             ON fd.figurine_id = f.id
   *     WHERE
   *         fd.distributor_id = 1
   * ) t
   * ORDER BY
   *     CASE release_status
   *         WHEN 'ANNOUNCED' THEN 1
   *         WHEN 'RELEASED'  THEN 2
   *         WHEN 'RUMORED'   THEN 3
   *         WHEN 'PROTOTYPE' THEN 4
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
          join f.distributors fd
          where fd.distributor.id = 1
          order by
            case
              when fd.releaseDate >= current_date then 1
              when current_date > fd.releaseDate then 2
              when fd.announcementDate is null then 3
              else 4
            end,
            case
              when fd.releaseDate >= current_date
                or current_date > fd.releaseDate
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
      countQuery =
          """
          select count(f)
          from Figurine f
          join f.distributors fd
          where fd.distributor.id = 1
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
