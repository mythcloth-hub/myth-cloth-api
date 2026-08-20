package com.mesofi.mythclothapi.figurines.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.repository.projection.FigurineCatalogSummaryProjection;
import com.mesofi.mythclothapi.figurines.repository.projection.FigurineReleaseYearSummaryProjection;

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
     * Retrieves summary counts for the figurine catalog.
     *
     * <p>
     * The summary includes the total number of figurines, the number of released
     * figurines, and the number of announced figurines.
     * </p>
     *
     * @return catalog summary projection with figurine totals
     */
    @Query(value = """
            SELECT
                SUM(CASE WHEN f.current_release_status IN ('RELEASED', 'ANNOUNCED') THEN 1 ELSE 0 END) AS totalFigurines,
                COALESCE(SUM(CASE WHEN f.current_release_status = 'RELEASED' THEN 1 ELSE 0 END), 0) AS totalReleased,
                COALESCE(SUM(CASE WHEN f.current_release_status = 'ANNOUNCED' THEN 1 ELSE 0 END), 0) AS totalAnnounced
            FROM figurines f
            """, nativeQuery = true)
    FigurineCatalogSummaryProjection getFigurineCatalogSummary();

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
}
