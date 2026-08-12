package com.mesofi.mythclothapi.figurineimports.csvsource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Provides access to the local figurine import CSV file used during integration
 * tests.
 *
 * <p>
 * This implementation is active only when the {@code integration} Spring
 * profile is enabled. The CSV file is loaded from the application's classpath
 * using UTF-8 encoding.
 * </p>
 */
@Component
@Profile("integration")
public class LocalFileImportCsvSource implements FigurineImportCsvSource {

    /**
     * Opens a reader for the local figurine import CSV fixture.
     *
     * @return a {@link Reader} for the figurine import CSV file
     * @throws IOException
     *             if the CSV file cannot be found, opened, or read
     */
    @Override
    public Reader openReader() throws IOException {
        ClassPathResource resource = new ClassPathResource("import/MythCloth Catalog - CatalogMyth.csv");

        return new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
    }
}
