package com.mesofi.mythclothapi.figurineimports;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.ClassPathResource;

import com.mesofi.mythclothapi.figurines.mapper.FigurineCsv;
import com.opencsv.bean.CsvToBeanBuilder;

/**
 * Utility class for loading figurine CSV fixtures used by tests.
 *
 * <p>
 * CSV fixtures are loaded from the {@code import/figurines} directory on the
 * test classpath and can either be returned as a {@link Reader} or parsed
 * directly into {@link FigurineCsv} instances.
 * </p>
 */
public final class FigurineImportMockLoader {

    private FigurineImportMockLoader() {
        // Utility class.
    }

    /**
     * Loads and parses a figurine CSV fixture from the test classpath.
     *
     * <p>
     * The fixture is read using UTF-8 encoding and parsed using OpenCSV into a list
     * of {@link FigurineCsv} instances.
     * </p>
     *
     * @param filename
     *            the name of the CSV fixture located under {@code import/figurines}
     * @return a list of {@link FigurineCsv} instances parsed from the fixture
     * @throws IOException
     *             if the fixture cannot be found, opened, or read
     */
    public static List<FigurineCsv> loadFigurinesCsv(String filename) throws IOException {
        try (Reader reader = loadImportCsvFixture(filename)) {

            return new CsvToBeanBuilder<FigurineCsv>(reader).withType(FigurineCsv.class)
                    .withIgnoreLeadingWhiteSpace(true).build().parse().stream().toList();

        } catch (IOException e) {
            throw new IOException("Unable to load figurines from " + filename, e);
        }
    }

    /**
     * Opens a figurine CSV fixture from the test classpath.
     *
     * <p>
     * The fixture is loaded from the {@code import/figurines} directory and the
     * returned reader uses UTF-8 encoding.
     * </p>
     *
     * <p>
     * The caller is responsible for closing the returned {@link Reader}.
     * </p>
     *
     * @param filename
     *            the name of the CSV fixture located under {@code import/figurines}
     * @return a reader for the requested CSV fixture
     * @throws IOException
     *             if the fixture cannot be found, opened, or read
     */
    public static Reader loadImportCsvFixture(String filename) throws IOException {
        ClassPathResource resource = new ClassPathResource("import/figurines/" + filename);
        return new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
    }
}
