package com.mesofi.mythclothapi.figurineimports.service;

import static com.mesofi.mythclothapi.catalogs.CatalogMockBuilder.createMockCatalogContext;
import static com.mesofi.mythclothapi.figurineimports.FigurineImportMockLoader.loadFigurinesCsv;
import static com.mesofi.mythclothapi.figurineimports.FigurineImportMockLoader.loadImportCsvFixture;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mesofi.mythclothapi.catalogs.CatalogService;
import com.mesofi.mythclothapi.catalogs.model.CatalogContext;
import com.mesofi.mythclothapi.config.MapperTestConfig;
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

@ActiveProfiles("test")
@SpringBootTest(classes = {FigurineImportService.class, MapperTestConfig.class})
public class FigurineImportServiceTest {

    @Autowired
    private FigurineImportService figurineImportService;
    @Autowired
    private FigurineMapper figurineMapper;

    @MockitoBean
    private CatalogService catalogService;
    @MockitoBean
    private FigurineService figurineService;
    @MockitoBean
    private FigurineRepository figurineRepository;
    @MockitoBean
    private FigurineImportCsvSource figurineImportCsvSource;
    @MockitoBean
    private FigurineImportHistoryService figurineImportHistoryService;
    @MockitoBean
    private FigurineImportRepository figurineImportRepository;

    @Test
    void importFromPublicDrive_shouldThrowFigurineImportException_whenCsvSourceOpenFails() throws IOException {

        IOException rootCause = new IOException("boom");
        when(figurineImportCsvSource.openReader()).thenThrow(rootCause);

        // Act + Assert
        assertThatThrownBy(() -> figurineImportService.importAllFigurinesFromPublicDrive())
                .isInstanceOf(FigurineImportException.class).hasMessage("There was an error importing figurines.");

        verify(figurineImportHistoryService).saveFigurineImport(0, "Unable to load all figurines: boom");
        verify(figurineService, never()).rebuildRestockHistory(any());
    }

    @Test
    void importFromPublicDrive_shouldThrowFigurineImportException_whenUnhandledExceptionOccurs() throws IOException {

        when(figurineImportCsvSource.openReader()).thenThrow(new RuntimeException("boom"));

        // Act + Assert
        assertThatThrownBy(() -> figurineImportService.importAllFigurinesFromPublicDrive())
                .isInstanceOf(FigurineImportException.class).hasMessage("There was an error importing figurines.");

        verify(figurineImportHistoryService).saveFigurineImport(0, "Unexpected error: boom");
        verify(figurineService, never()).rebuildRestockHistory(any());
    }

    @Test
    void importFromPublicDrive_shouldSaveAllFigurines_whenAllFigurinesAreExisting() throws IOException {
        // Arrange
        String filename = "MythCloth Catalog - CatalogMyth.csv";

        CatalogContext catalogContext = createMockCatalogContext();
        List<FigurineCsv> figurinesCsv = loadFigurinesCsv(filename);
        List<Figurine> figurines = figurinesCsv.stream().map(csv -> figurineMapper.toFigurine(csv, catalogContext))
                .toList();

        List<Figurine> existingFigurines = getExistingFigurines(figurines.subList(0, figurines.size()));

        when(catalogService.retrieveCatalogContext()).thenReturn(catalogContext);
        when(figurineImportCsvSource.openReader()).thenReturn(loadImportCsvFixture(filename));
        when(figurineRepository
                .findByLegacyNameInOrderById(figurinesCsv.stream().map(FigurineCsv::getOriginalName).toList()))
                .thenReturn(existingFigurines);
        when(figurineService.initializeFigurineForUpdate(any(Figurine.class), any(Figurine.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));
        when(figurineRepository.saveAllAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        figurineImportService.importAllFigurinesFromPublicDrive();

        // Verify
        verify(figurineImportHistoryService).saveFigurineImport(12, null);
        verify(figurineService).rebuildRestockHistory(any());
        verify(catalogService).retrieveCatalogContext();
        verify(figurineImportCsvSource).openReader();
        verify(figurineRepository)
                .findByLegacyNameInOrderById(figurinesCsv.stream().map(FigurineCsv::getOriginalName).toList());
        verify(figurineService, times(12)).initializeFigurineForUpdate(any(Figurine.class), any(Figurine.class));
        verify(figurineRepository).saveAllAndFlush(any());
    }

    @Test
    void importFromPublicDrive_shouldSaveAllFigurines_whenAllFigurinesAreNewOrExisting() throws IOException {
        // Arrange
        String filename = "MythCloth Catalog - CatalogMyth.csv";

        CatalogContext catalogContext = createMockCatalogContext();
        List<FigurineCsv> figurinesCsv = loadFigurinesCsv(filename);
        List<Figurine> figurines = figurinesCsv.stream().map(csv -> figurineMapper.toFigurine(csv, catalogContext))
                .toList();

        List<Figurine> existingFigurines = getExistingFigurines(figurines.subList(3, 6));

        when(catalogService.retrieveCatalogContext()).thenReturn(catalogContext);
        when(figurineImportCsvSource.openReader()).thenReturn(loadImportCsvFixture(filename));
        when(figurineRepository
                .findByLegacyNameInOrderById(figurinesCsv.stream().map(FigurineCsv::getOriginalName).toList()))
                .thenReturn(existingFigurines);
        when(figurineService.initializeFigurineForCreate(any(Figurine.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(figurineService.initializeFigurineForUpdate(any(Figurine.class), any(Figurine.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));
        when(figurineRepository.saveAllAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        figurineImportService.importAllFigurinesFromPublicDrive();

        // Verify
        verify(figurineImportHistoryService).saveFigurineImport(12, null);
        verify(figurineService).rebuildRestockHistory(any());
        verify(catalogService).retrieveCatalogContext();
        verify(figurineImportCsvSource).openReader();
        verify(figurineRepository)
                .findByLegacyNameInOrderById(figurinesCsv.stream().map(FigurineCsv::getOriginalName).toList());
        verify(figurineService, times(9)).initializeFigurineForCreate(any(Figurine.class));
        verify(figurineService, times(3)).initializeFigurineForUpdate(any(Figurine.class), any(Figurine.class));
        verify(figurineRepository).saveAllAndFlush(any());
    }

    @Test
    void getAllFigurineImports_shouldRetrieveAllFigurines_whenAllFigurinesAreAvailable() {
        // Arrange
        when(figurineImportRepository.findAll()).thenReturn(getExistingImports());

        // Act
        List<FigurineImportResp> figurineImports = figurineImportService.getAllFigurineImports();

        // Verify
        assertEquals(3, figurineImports.size());
        assertEquals(15, figurineImports.get(0).imported());
        assertEquals(10, figurineImports.get(1).imported());
        assertEquals(12, figurineImports.get(2).imported());

        verify(figurineImportRepository).findAll();
    }

    private List<Figurine> getExistingFigurines(List<Figurine> figurines) {
        for (int i = 1; i < figurines.size(); i++) {
            figurines.get(i).setId((long) i);
        }
        return figurines;
    }

    private List<FigurineImport> getExistingImports() {
        FigurineImport import1 = new FigurineImport();
        import1.setId(1L);
        import1.setTotalImported(12);
        import1.setErrorMessage(null);
        import1.setCompletedAt(Instant.parse("2024-06-01T10:15:30.00Z"));

        FigurineImport import2 = new FigurineImport();
        import2.setId(2L);
        import2.setTotalImported(10);
        import2.setErrorMessage("Some error occurred");
        import2.setCompletedAt(Instant.parse("2024-06-02T11:20:35.00Z"));

        FigurineImport import3 = new FigurineImport();
        import3.setId(3L);
        import3.setTotalImported(15);
        import3.setErrorMessage(null);
        import3.setCompletedAt(Instant.parse("2024-06-03T12:25:40.00Z"));

        return List.of(import1, import2, import3);
    }
}
