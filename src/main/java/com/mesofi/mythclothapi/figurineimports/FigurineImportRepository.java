package com.mesofi.mythclothapi.figurineimports;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for accessing figurine import history.
 *
 * <p>
 * Provides persistence operations for {@link FigurineImport} records, including
 * storing and retrieving the results of figurine import operations.
 * </p>
 */
@Repository
public interface FigurineImportRepository extends JpaRepository<FigurineImport, Long> {

}
