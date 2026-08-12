package com.mesofi.mythclothapi.figurineimports.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mesofi.mythclothapi.figurineimports.FigurineImport;
import com.mesofi.mythclothapi.figurineimports.FigurineImportRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service responsible for persisting figurine import execution history.
 *
 * <p>
 * Import history is persisted in an independent transaction so that the result
 * of an import operation is retained even when the transaction performing the
 * import is rolled back due to an error.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class FigurineImportHistoryService {

    private final FigurineImportRepository figurineImportRepository;

    /**
     * Persists a record containing the result of a figurine import operation.
     *
     * <p>
     * The operation runs in a new transaction using
     * {@link Propagation#REQUIRES_NEW}. This ensures that the import history record
     * is committed independently of the transaction performing the figurine import.
     * </p>
     *
     * @param totalImported
     *            the total number of figurines successfully imported
     * @param errorMessage
     *            the error message generated during the import, or {@code null} if
     *            the import completed successfully
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFigurineImport(int totalImported, String errorMessage) {

        FigurineImport figurineImport = new FigurineImport();

        figurineImport.setCompletedAt(Instant.now());
        figurineImport.setErrorMessage(errorMessage);
        figurineImport.setTotalImported(totalImported);

        figurineImportRepository.save(figurineImport);
    }
}
