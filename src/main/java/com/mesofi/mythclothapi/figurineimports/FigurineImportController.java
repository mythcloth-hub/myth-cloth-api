package com.mesofi.mythclothapi.figurineimports;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mesofi.mythclothapi.figurineimports.service.FigurineImportService;
import com.mesofi.mythclothapi.security.permissions.model.Permissions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for managing figurine import operations and their history.
 *
 * <p>
 * Provides endpoints for triggering a bulk figurine import and retrieving the
 * results of previous import operations.
 * </p>
 *
 * <p>
 * Import operations require the {@code figurines:load} authority.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/figurines")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('" + Permissions.FIGURINES_IMPORT + "')")
public class FigurineImportController {

    private final FigurineImportService figurineImportService;

    /**
     * Triggers a bulk import of figurines from the configured public CSV source.
     *
     * <p>
     * The import process reads the CSV source, resolves the required catalog
     * references, and creates new figurines or updates existing figurines
     * identified by their legacy name. Following a successful import, restock
     * history is rebuilt for the imported figurines.
     * </p>
     *
     * <p>
     * The import is executed synchronously as part of the request. A
     * {@code 202 Accepted} response is returned after the import operation has been
     * successfully initiated and completed by the service.
     * </p>
     *
     * @return a {@link ResponseEntity} with status {@code 202 Accepted} and no
     *         response body
     */
    @PostMapping("/load")
    public ResponseEntity<Void> loadAllFigurines() {
        log.info("Loading all figurines ...");

        figurineImportService.importAllFigurinesFromPublicDrive();
        return ResponseEntity.accepted().build();
    }

    /**
     * Retrieves the most recent figurine import history.
     *
     * <p>
     * Each entry represents an individual import execution and includes information
     * such as the completion time, number of figurines imported, and any error
     * encountered during the operation.
     * </p>
     *
     * <p>
     * The service limits the result to the most recent import records and requires
     * the {@code figurines:load} authority.
     * </p>
     *
     * @return a list of {@link FigurineImportResp} records ordered from the most
     *         recent import to the oldest
     */
    @GetMapping("/imports")
    public List<FigurineImportResp> getFigurineImports() {
        log.info("Retrieving all figurine imports ...");

        return figurineImportService.getAllFigurineImports();
    }
}
