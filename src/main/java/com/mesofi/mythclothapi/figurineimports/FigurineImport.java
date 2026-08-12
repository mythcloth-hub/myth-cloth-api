package com.mesofi.mythclothapi.figurineimports;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.mesofi.mythclothapi.common.Auditable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a record of a figurine import execution.
 *
 * <p>
 * Stores summary information about an import operation, including the number of
 * figurines successfully imported, any error encountered during the operation,
 * and the time at which the operation completed.
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
     * The total number of figurines successfully imported during the operation.
     */
    @Column(nullable = false)
    private int totalImported;

    /**
     * The error message produced during the import, or {@code null} if no error
     * occurred.
     */
    @Column(length = 1000)
    private String errorMessage;

    /**
     * The date and time at which the import operation completed.
     */
    @Column(nullable = false)
    private Instant completedAt;
}
