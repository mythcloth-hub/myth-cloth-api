package com.mesofi.mythclothapi.figurineimports;

import static com.mesofi.mythclothapi.catalogs.CatalogService.CATALOG_CONTEXT_CACHE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import com.mesofi.mythclothapi.it.ControllerBaseIT;

@Sql(scripts = "/seed-catalogs.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-figurine-import-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class FigurineImportControllerIT extends ControllerBaseIT {

    private static final Logger log = LoggerFactory.getLogger(FigurineImportControllerIT.class);

    private static final String FIGURINE_MAPPING = "/figurines";
    private static final String FIGURINE_LOAD_ENDPOINT = FIGURINE_MAPPING + "/load";
    private static final String FIGURINE_IMPORT_ENDPOINT = FIGURINE_MAPPING + "/imports";

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    public void setUp() {
        Cache cache = cacheManager.getCache(CATALOG_CONTEXT_CACHE);
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    @DisplayName("Should load figurines and expose import history")
    void shouldLoadFigurinesAndExposeImportHistory() {
        log.info("Starting figurine import flow...");
        ResponseEntity<Void> responseLoad = rest.post().uri(FIGURINE_LOAD_ENDPOINT).retrieve().toEntity(Void.class);
        log.info("Figurine import flow completed with status: {}", responseLoad.getStatusCode());

        ResponseEntity<List<FigurineImportResp>> responseImport = rest.get().uri(FIGURINE_IMPORT_ENDPOINT).retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });

        assertThat(responseImport.getStatusCode()).isEqualTo(OK);
        assertThat(responseImport.getBody()).isNotNull();

        List<FigurineImportResp> importRespList = responseImport.getBody();
        assertThat(importRespList).isNotEmpty();
        assertThat(importRespList).allSatisfy(record -> {
            assertThat(record.imported()).isPositive();
            assertThat(record.errorMessage()).isNull();
        });
    }
}
