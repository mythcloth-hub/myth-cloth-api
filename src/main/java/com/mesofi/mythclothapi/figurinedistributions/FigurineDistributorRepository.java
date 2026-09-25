package com.mesofi.mythclothapi.figurinedistributions;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing FigurineDistributor entities.
 */
@Repository
public interface FigurineDistributorRepository extends JpaRepository<FigurineDistributor, Long> {

    @Query(value = """
            SELECT
                fd.release_date,
                fd.release_date_confirmed,
                fd.announcement_date,
                d.country as country_code
            FROM figurine_distributor fd
            JOIN distributors d ON d.id = fd.distributor_id
            WHERE fd.figurine_id = :figurineId
            """, nativeQuery = true)
    List<FigurineDistributorProjection> findByFigurineId(Long figurineId);
}
