package com.mesofi.mythclothapi.figurines.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.figurines.model.FigurineImport;

/**
 * Repository interface for {@link FigurineImport} persistence operations.
 *
 * <p>
 * Provides standard CRUD operations for managing figurine import records,
 * including saving, retrieving, updating, and deleting import history.
 * </p>
 */
@Repository
public interface FigurineImportRepository extends JpaRepository<FigurineImport, Long> {

}
