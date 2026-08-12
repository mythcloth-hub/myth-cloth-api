package com.mesofi.mythclothapi.figurineimports.service;

import static com.mesofi.mythclothapi.collectorscollections.CollectorCollectionFigurineService.COLLECTOR_FIGURINE_CACHE;
import static com.mesofi.mythclothapi.figurines.FigurineService.FIGURINE_CACHE;
import static com.mesofi.mythclothapi.figurines.FigurineService.FIGURINE_SUMMARY_CACHE;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.catalogs.CatalogService;
import com.mesofi.mythclothapi.catalogs.model.CatalogContext;
import com.mesofi.mythclothapi.figurineimports.FigurineImport;
import com.mesofi.mythclothapi.figurineimports.FigurineImportException;
import com.mesofi.mythclothapi.figurineimports.FigurineImportRepository;
import com.mesofi.mythclothapi.figurineimports.FigurineImportResp;
import com.mesofi.mythclothapi.figurineimports.csvsource.FigurineImportCsvSource;
import com.mesofi.mythclothapi.figurines.FigurineService;
import com.mesofi.mythclothapi.figurines.mapper.FigurineCsv;
import com.mesofi.mythclothapi.figurines.mapper.FigurineMapper;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;
import com.opencsv.bean.CsvToBeanBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for importing figurines from the public CSV source and
 * maintaining figurine import history.
 *
 * <p>
 * The import process reads figurine data from the configured CSV source,
 * resolves the required catalog references, and determines whether each
 * figurine should be created or updated based on its unique legacy name.
 * </p>
 *
 * <p>
 * After the figurines are persisted, restock history is rebuilt for the
 * imported records. Relevant application caches are evicted whenever an import
 * is executed to ensure subsequent requests use the latest figurine data.
 * </p>
 *
 * <p>
 * Import execution history is persisted independently through
 * {@link FigurineImportHistoryService}, allowing the result of an import to be
 * recorded even when the main import transaction is rolled back.
 * </p>
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class FigurineImportService {

    private final CatalogService catalogService;
    private final FigurineService figurineService;
    private final FigurineRepository figurineRepository;
    private final FigurineMapper figurineMapper;
    private final FigurineImportCsvSource figurineImportCsvSource;
    private final FigurineImportHistoryService figurineImportHistoryService;
    private final FigurineImportRepository figurineImportRepository;

    /**
     * Imports all figurines from the configured public CSV source.
     *
     * <p>
     * For each CSV record, the method determines whether a figurine already exists
     * by its legacy name. New figurines are initialized for creation, while
     * existing figurines are initialized for update. All processed figurines are
     * then persisted in a single transaction.
     * </p>
     *
     * <p>
     * If the import fails, the transaction is rolled back and a
     * {@link FigurineImportException} is thrown. Regardless of the outcome, the
     * import result is recorded through {@link FigurineImportHistoryService}.
     * </p>
     *
     * <p>
     * After a successful import, restock history is rebuilt for all imported
     * figurines. Figurine-related caches are evicted when the import operation
     * begins.
     * </p>
     *
     * @throws FigurineImportException
     *             if the CSV cannot be read or an unexpected error occurs during
     *             the import
     */
    @Transactional
    @CacheEvict(value = {FIGURINE_CACHE, FIGURINE_SUMMARY_CACHE, COLLECTOR_FIGURINE_CACHE}, allEntries = true)
    public void importAllFigurinesFromPublicDrive() {
        log.info("Importing all figurines from public drive...");

        List<Figurine> importedFigurines;
        List<Figurine> allFigurines = new ArrayList<>();

        CatalogContext catalogContext = catalogService.retrieveCatalogContext();

        String errorMessage = null;
        int totalImported = 0;

        try (Reader reader = figurineImportCsvSource.openReader()) {
            List<FigurineCsv> csvRows = new CsvToBeanBuilder<FigurineCsv>(reader).withType(FigurineCsv.class)
                    .withIgnoreLeadingWhiteSpace(true).build().parse();

            log.info("Importing {} figurines from CSV file.", csvRows.size());

            Map<String, Figurine> existingFigurinesByLegacyName = figurineRepository
                    .findByLegacyNameInOrderById(csvRows.stream().map(FigurineCsv::getOriginalName).toList()).stream()
                    .collect(Collectors.toMap(Figurine::getLegacyName, Function.identity()));

            Figurine newOrExisting;

            for (FigurineCsv csv : csvRows) {
                if (isNewFigurine(existingFigurinesByLegacyName, csv.getOriginalName())) {
                    newOrExisting = figurineService
                            .initializeFigurineForCreate(figurineMapper.toFigurine(csv, catalogContext));
                } else {
                    Figurine existing = existingFigurinesByLegacyName.get(csv.getOriginalName());
                    Figurine incoming = figurineMapper.toFigurine(csv, catalogContext);
                    newOrExisting = figurineService.initializeFigurineForUpdate(existing, incoming);
                }

                allFigurines.add(newOrExisting);
                totalImported++;
            }

            importedFigurines = figurineRepository.saveAllAndFlush(allFigurines);

        } catch (IOException ex) {
            log.error("Error while reading csv file.", ex);
            errorMessage = "Unable to load all figurines: " + ex.getMessage();
            throw new FigurineImportException();

        } catch (Exception ex) {
            log.error("Unexpected error while importing figurines.", ex);
            errorMessage = "Unexpected error: " + ex.getMessage();
            throw new FigurineImportException();

        } finally {
            figurineImportHistoryService.saveFigurineImport(totalImported, errorMessage);
        }

        // Rebuild restock history for all imported figurines.
        figurineService.rebuildRestockHistory(importedFigurines);
    }

    /**
     * Retrieves the most recent figurine import records.
     *
     * <p>
     * At most 20 records are returned, ordered from the most recently completed
     * import to the oldest.
     * </p>
     *
     * <p>
     * The retrieved entities are mapped to {@link FigurineImportResp} instances
     * before being returned.
     * </p>
     *
     * @return a list containing up to 20 figurine import records, ordered by
     *         completion time in descending order
     */
    public List<FigurineImportResp> getAllFigurineImports() {
        return figurineImportRepository.findAll().stream()
                .sorted(Comparator.comparing(FigurineImport::getCompletedAt).reversed()).limit(20)
                .map(figurineMapper::toFigurineImportResp).toList();
    }

    /**
     * Determines whether a figurine from the CSV source does not already exist in
     * the database.
     *
     * @param figurinesByLegacyName
     *            existing figurines indexed by legacy name
     * @param originalName
     *            the legacy name of the figurine being processed
     * @return {@code true} if no existing figurine has the specified legacy name;
     *         {@code false} otherwise
     */
    private boolean isNewFigurine(Map<String, Figurine> figurinesByLegacyName, String originalName) {

        return !figurinesByLegacyName.containsKey(originalName);
    }
}
