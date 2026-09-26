package com.mesofi.mythclothapi.figurines.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.repository.projection.FigurineReleaseYearSummaryProjection;
import com.mesofi.mythclothapi.figurines.repository.projection.FigurineRestockProjection;

/**
 * Repository for {@link Figurine} persistence and query operations.
 *
 * <p>
 * Extends {@link JpaRepository} to provide standard CRUD operations and
 * {@link FigurineQueryRepository} to provide custom figurine search and
 * retrieval operations.
 * </p>
 *
 * <p>
 * This repository provides operations for:
 * </p>
 * <ul>
 * <li>Finding figurines by legacy names.</li>
 * <li>Finding figurines updated before a specified timestamp.</li>
 * <li>Retrieving figurines belonging to a specific {@link LineUp}.</li>
 * <li>Retrieving released or announced figurines ordered by their first release
 * date.</li>
 * <li>Clearing previous-release relationships between figurines.</li>
 * </ul>
 *
 * @see Figurine
 * @see FigurineQueryRepository
 * @see JpaRepository
 */
@Repository
public interface FigurineRepository extends JpaRepository<Figurine, Long>, FigurineQueryRepository {

    /**
     * Retrieves the restock history for a specific figurine, including all previous
     * releases in the chain.
     *
     * <p>
     * The query uses a recursive common table expression (CTE) to traverse the
     * previous-release relationships and gather all related figurines. For each
     * figurine in the chain, only the first distributor record is considered when
     * determining the release date.
     * </p>
     *
     * @param figurineId
     *            the ID of the figurine for which to retrieve restock history
     * @return a list of projections containing the IDs and release dates of all
     *         previous releases in the chain, ordered by their level in the chain
     */
    @Query(value = """
            WITH RECURSIVE release_chain (
                id,
                previous_release_id,
                release_date,
                level
            ) AS (
                SELECT
                    f.id,
                    f.previous_release_id,
                    fd.release_date,
                    1 AS level
                FROM figurines f
                LEFT JOIN (
                    SELECT *
                    FROM (
                        SELECT
                            fd.*,
                            ROW_NUMBER() OVER (
                                PARTITION BY figurine_id
                                ORDER BY id
                            ) AS rn
                        FROM figurine_distributor fd
                    ) x
                    WHERE rn = 1
                ) fd
                    ON fd.figurine_id = f.id
                WHERE f.id = :figurineId

                UNION ALL

                SELECT
                    f.id,
                    f.previous_release_id,
                    fd.release_date,
                    rc.level + 1
                FROM figurines f
                LEFT JOIN (
                    SELECT *
                    FROM (
                        SELECT
                            fd.*,
                            ROW_NUMBER() OVER (
                                PARTITION BY figurine_id
                                ORDER BY id
                            ) AS rn
                        FROM figurine_distributor fd
                    ) x
                    WHERE rn = 1
                ) fd
                    ON fd.figurine_id = f.id
                JOIN release_chain rc
                    ON f.id = rc.previous_release_id
            )
            SELECT
                id,
                release_date
            FROM release_chain
            WHERE level > 1
            ORDER BY level
            """, nativeQuery = true)
    List<FigurineRestockProjection> findRestockHistoryByFigurineId(Long figurineId);

    /**
     * Finds figurines whose legacy name matches one of the specified names.
     *
     * @param legacyNames
     *            legacy names used to identify figurines
     * @return figurines matching the specified legacy names, ordered by ID
     */
    List<Figurine> findByLegacyNameInOrderById(List<String> legacyNames);

    /**
     * Finds all figurines belonging to the specified lineup.
     *
     * @param lineUP
     *            the lineup associated with the figurines
     * @return figurines belonging to the specified lineup
     */
    List<Figurine> findAllByLineup(LineUp lineUP);

    /**
     * Retrieves released or announced figurines that are not associated with an
     * anniversary release.
     *
     * <p>
     * Results are ordered by the earliest distributor release date in descending
     * order, placing the most recently released figurines first.
     * </p>
     *
     * @return released or announced figurines ordered by their first release date
     *         in descending order
     */
    @Query("""
            SELECT f
            FROM Figurine f
            JOIN f.distributors fd
            WHERE (f.currentReleaseStatus = 'RELEASED' or f.currentReleaseStatus = 'ANNOUNCED')
            AND f.anniversary IS NULL
            GROUP BY f
            ORDER BY MIN(fd.releaseDate) DESC
            """)
    List<Figurine> findReleasedOrAnnouncedOrderByFirstReleaseDateDesc();

    /**
     * Removes the previous-release relationship from all figurines that currently
     * have one.
     *
     * <p>
     * This operation does not delete the referenced figurines; it only clears the
     * {@code previousRelease} association.
     * </p>
     *
     * @return the number of figurines whose previous-release relationship was
     *         cleared
     */
    @Modifying
    @Query("""
            UPDATE Figurine f
            SET f.previousRelease = null
            WHERE f.previousRelease IS NOT NULL
            """)
    int clearPreviousReleases();

    /**
     * Retrieves a summary of released figurines grouped by release year and lineup.
     * For each figurine, only the first distributor record is considered when
     * determining the release date.
     *
     * @return projections containing the release year, lineup description, and
     *         number of released figurines for each year and lineup
     */
    @Query(value = """
            SELECT
                CAST(EXTRACT(YEAR FROM fd.release_date) AS INTEGER) AS releaseYear,
                l.description AS lineupDescription,
                COUNT(*) AS figurineCount
            FROM figurines f
            LEFT JOIN (
                SELECT *
                FROM (
                    SELECT
                        fd.*,
                        ROW_NUMBER() OVER (
                            PARTITION BY figurine_id
                            ORDER BY id
                        ) AS rn
                    FROM figurine_distributor fd
                ) x
                WHERE rn = 1
            ) fd
                ON fd.figurine_id = f.id
            JOIN lineups l
                ON l.id = f.lineup_id
            WHERE f.current_release_status = 'RELEASED'
            GROUP BY
                CAST(EXTRACT(YEAR FROM fd.release_date) AS INTEGER),
                l.description
            ORDER BY
                releaseYear,
                lineupDescription
            """, nativeQuery = true)
    List<FigurineReleaseYearSummaryProjection> getReleaseYearSummary();

    /**
     * Retrieves the IDs of all figurines that are either released or announced.
     *
     * <p>
     * For each figurine, only the first distributor record is considered when
     * determining the release date.
     * </p>
     *
     * @return a list of IDs for figurines with released or announced status
     */
    @Query(value = """
             SELECT
                 f.id
             FROM figurines f
             LEFT JOIN (
                 SELECT *
                 FROM (
                     SELECT
                         fd.*,
                         ROW_NUMBER() OVER (
                             PARTITION BY figurine_id
                             ORDER BY id
                         ) AS rn
                     FROM figurine_distributor fd
                 ) x
                 WHERE rn = 1
            ) fd
                ON fd.figurine_id = f.id
            JOIN lineups l
                ON l.id = f.lineup_id
            WHERE f.current_release_status IN ('RELEASED', 'ANNOUNCED')\s
            ORDER by f.id ;
            """, nativeQuery = true)
    List<Long> findAllFigurineIdsWithReleasedOrAnnouncedStatus();
}
