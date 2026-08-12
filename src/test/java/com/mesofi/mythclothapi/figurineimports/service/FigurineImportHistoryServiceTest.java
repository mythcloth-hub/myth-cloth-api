package com.mesofi.mythclothapi.figurineimports.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mesofi.mythclothapi.figurineimports.FigurineImport;
import com.mesofi.mythclothapi.figurineimports.FigurineImportRepository;

@ActiveProfiles("test")
@SpringBootTest(classes = FigurineImportHistoryService.class)
public class FigurineImportHistoryServiceTest {

    @Autowired
    private FigurineImportHistoryService figurineImportHistoryService;

    @MockitoBean
    private FigurineImportRepository figurineImportRepository;

    @Test
    void saveFigurineImport_shouldPersistImportHistory() {
        int totalImported = 42;
        String errorMessage = "Something went wrong";

        figurineImportHistoryService.saveFigurineImport(totalImported, errorMessage);

        var captor = org.mockito.ArgumentCaptor.forClass(FigurineImport.class);
        verify(figurineImportRepository).save(captor.capture());

        FigurineImport saved = captor.getValue();
        assertEquals(totalImported, saved.getTotalImported());
        assertEquals(errorMessage, saved.getErrorMessage());
        assertNotNull(saved.getCompletedAt());
    }
}
