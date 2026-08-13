package com.mesofi.mythclothapi.figurines.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.figurines.model.Figurine;

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
     * Finds figurines whose legacy name matches one of the specified names and
     * whose last update occurred before the specified timestamp.
     *
     * @param legacyNames
     *            legacy names used to identify figurines
     * @param updateDate
     *            timestamp used as the upper bound for the last update date
     * @return figurines matching the specified names and update-date criteria,
     *         ordered by ID
     */
    List<Figurine> findByLegacyNameInAndUpdateDateLessThanOrderById(List<String> legacyNames, Instant updateDate);

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
}