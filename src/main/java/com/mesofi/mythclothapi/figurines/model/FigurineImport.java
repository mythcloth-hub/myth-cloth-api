package com.mesofi.mythclothapi.figurines.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.mesofi.mythclothapi.common.Auditable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the result of a figurine import operation.
 *
 * <p>
 * Stores summary information about the import, including the number of
 * figurines successfully imported, any error message produced during the
 * operation, and the date and time when the import completed.
 * </p>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "figurine_imports")
public class FigurineImport extends Auditable {

    /**
     * The total number of figurines successfully imported.
     */
    @Column(nullable = false)
    private int totalImported;

    /**
     * The total number of figurines that were skipped during the import.
     */
    @Column(nullable = false)
    private int totalSkipped;

    /**
     * The error message generated during the import, if applicable.
     */
    private String errorMessage;

    /**
     * The date and time when the import operation completed.
     */
    @Column(nullable = false)
    private LocalDateTime completedAt;
}
