package com.mesofi.mythclothapi.figurineimports.csvsource;

import java.io.IOException;
import java.io.Reader;

/**
 *
 * Provides access to the CSV source used for figurine imports.
 *
 * <p>
 * Implementations are responsible for opening and returning a {@link Reader}
 * for the source containing figurine import data.
 * </p>
 *
 * <p>
 * The source may be backed by a local file, a remote resource, or another data
 * provider, allowing the import process to remain independent of the underlying
 * source.
 * </p>
 *
 */
@FunctionalInterface
public interface FigurineImportCsvSource {

    /**
     * Opens a reader for the figurine import CSV source.
     *
     * @return a {@link Reader} for the figurine import CSV data
     * @throws IOException
     *             if the source cannot be opened or accessed
     */
    Reader openReader() throws IOException;
}
