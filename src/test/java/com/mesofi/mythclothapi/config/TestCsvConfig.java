package com.mesofi.mythclothapi.config;

import java.io.InputStreamReader;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import com.mesofi.mythclothapi.figurines.imports.FigurineCsvSource;

/**
 * Test-only configuration that provides a deterministic CSV source fixture for figurine imports.
 */
@TestConfiguration
public class TestCsvConfig {

  /**
   * Creates a {@link FigurineCsvSource} backed by the catalog CSV file bundled in test resources.
   *
   * @return CSV source for import-related tests
   */
  @Bean
  public FigurineCsvSource testCsvSource() {
    return () -> {
      ClassPathResource resource =
          new ClassPathResource("import/figurines/MythCloth Catalog - CatalogMyth.csv");
      return new InputStreamReader(resource.getInputStream());
    };
  }
}
