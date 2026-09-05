package com.mesofi.mythclothapi.stats;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.figurines.model.Figurine;

/**
 * Repository interface for accessing figurine statistics.
 *
 * <p>
 * This repository provides methods to retrieve statistics related to released
 * figurines, including their names, prices, and release dates. It uses a native
 * SQL query to fetch the required data from the database.
 * </p>
 *
 * <p>
 * The repository extends {@link JpaRepository} to leverage Spring Data JPA's
 * capabilities for CRUD operations and query execution.
 * </p>
 */
@Repository
public interface StatisticsRepository extends JpaRepository<Figurine, Long> {

    /**
     * Retrieves a list of released figurines along with their statistics.
     *
     * <p>
     * The query selects the figurine ID, normalized name, price, and release date
     * for all figurines that have a current release status of 'RELEASED'. It uses a
     * left join to include the first distributor record for each figurine, if
     * available.
     * </p>
     *
     * @return a list of {@link StatisticsReleasedFigurineProjection} containing the
     *         released figurine statistics
     */
    @Query(value = """
            SELECT
                f.id,
                f.display_name AS name,
                fd.price,
                fd.release_date
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
            WHERE f.current_release_status = 'RELEASED'
            """, nativeQuery = true)
    List<StatisticsReleasedFigurineProjection> findReleasedFigurineStatistics();

    /**
     * Retrieves a list of official images associated with a specific figurine.
     *
     * <p>
     * The query selects the official images for the figurine identified by the
     * provided ID. It returns a list of image URLs or paths.
     * </p>
     *
     * @param id
     *            the ID of the figurine for which to retrieve official images
     * @return a list of strings representing the official images associated with
     *         the specified figurine
     */
    @Query(value = """
            SELECT
                oi.official_images
            FROM official_images oi
            WHERE oi.figurine_id = :id
            """, nativeQuery = true)
    List<String> findOfficialImagesStatistics(Long id);
}
