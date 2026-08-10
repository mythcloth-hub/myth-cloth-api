package com.mesofi.mythclothapi.figurines;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mesofi.mythclothapi.figurines.model.FigurineImport;
import com.mesofi.mythclothapi.figurines.repository.FigurineImportRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service responsible for persisting figurine import history.
 *
 * <p>
 * Import history is persisted in a separate transaction so that the import
 * result is recorded even when the main figurine import transaction is rolled
 * back due to an error.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class FigurineImportHistoryService {

    private final FigurineImportRepository figurineImportRepository;

    /**
     * Saves a record containing the result of a figurine import operation.
     *
     * <p>
     * The operation runs in a new transaction using
     * {@link Propagation#REQUIRES_NEW}, ensuring that the import history record is
     * committed independently of the transaction that performs the figurine import.
     * </p>
     *
     * @param totalImported
     *            the total number of figurines successfully imported
     * @param totalSkipped
     *            the total number of figurines skipped during the import
     * @param errorMessage
     *            the error message generated during the import, or {@code null} if
     *            the import completed successfully
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFigurineImport(int totalImported, int totalSkipped, String errorMessage) {

        FigurineImport figurineImport = new FigurineImport();

        figurineImport.setCompletedAt(LocalDateTime.now());
        figurineImport.setErrorMessage(errorMessage);
        figurineImport.setTotalImported(totalImported);
        figurineImport.setTotalSkipped(totalSkipped);

        figurineImportRepository.save(figurineImport);
    }
}
