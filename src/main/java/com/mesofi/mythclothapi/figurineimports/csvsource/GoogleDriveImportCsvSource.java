package com.mesofi.mythclothapi.figurineimports.csvsource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mesofi.mythclothapi.figurineimports.config.FigurineImportProperties;

import lombok.RequiredArgsConstructor;

/**
 * Provides access to the figurine import CSV file hosted on Google Drive.
 *
 * <p>
 * The CSV source URL is built from the configured
 * {@link FigurineImportProperties}. A new {@link Reader} is opened for each
 * import operation, allowing the import service to consume the remote CSV
 * directly without storing it locally.
 * </p>
 */
@Component
@Profile("!integration")
@RequiredArgsConstructor
public class GoogleDriveImportCsvSource implements FigurineImportCsvSource {

    private final FigurineImportProperties properties;

    /**
     * Opens a reader for the configured Google Drive CSV file.
     *
     * @return a {@link Reader} for the remote figurine import CSV
     * @throws IOException
     *             if the CSV URL cannot be opened or the remote resource cannot be
     *             accessed
     */
    @Override
    public Reader openReader() throws IOException {
        String url = properties.buildUrl();
        return new InputStreamReader(URI.create(url).toURL().openStream());
    }
}
