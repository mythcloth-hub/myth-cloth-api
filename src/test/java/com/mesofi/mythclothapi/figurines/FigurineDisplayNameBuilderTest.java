package com.mesofi.mythclothapi.figurines;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import com.mesofi.mythclothapi.anniversaries.model.Anniversary;
import com.mesofi.mythclothapi.anniversaries.model.AnniversaryType;
import com.mesofi.mythclothapi.catalogs.model.Group;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.model.Figurine;

/**
 * Parameterized tests for {@link FigurineDisplayNameBuilder}.
 * <p>
 * The expected display names are defined in an external CSV file that
 * represents the official Myth Cloth catalog. Each row is converted into a
 * minimal {@link Figurine} instance containing only the attributes that
 * influence display name generation, such as:
 * <ul>
 * <li>Normalized name</li>
 * <li>{@link LineUp}</li>
 * <li>{@link Series}</li>
 * <li>{@link Group}</li>
 * <li>Release date</li>
 * <li>Special edition flags (OCE, Revival, Broken, Golden, Gold, Manga,
 * Set)</li>
 * <li>{@link Anniversary} information</li>
 * </ul>
 * <p>
 * The generated display name is then compared with the expected value from the
 * catalog to ensure that {@link FigurineDisplayNameBuilder} follows the
 * official naming conventions.
 */
public class FigurineDisplayNameBuilderTest {

    private static final DateTimeFormatter FULL_DATE = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US);

    private static final DateTimeFormatter MONTH_YEAR = DateTimeFormatter.ofPattern("M/yyyy", Locale.US);

    /**
     * Verifies that {@link FigurineDisplayNameBuilder} generates the expected
     * display name for each figurine defined in the test catalog.
     * <p>
     * Each row in the CSV file represents a test case containing the figurine
     * attributes required to construct a minimal {@link Figurine} instance and the
     * expected display name.
     *
     * @param _mythClothOriginalName
     *            original name from the Myth Cloth catalog (used only as the test
     *            display name)
     * @param baseName
     *            normalized figurine name
     * @param displayName
     *            expected display name
     * @param releaseJPY
     *            Japanese release date in {@code M/d/yyyy} or {@code M/yyyy} format
     * @param lineUp
     *            figurine line-up
     * @param series
     *            figurine series
     * @param group
     *            figurine group
     * @param _metal
     *            catalog value not used by this test. Column exist only to preserve
     *            the file format and are not part of the test logic
     * @param oce
     *            whether the figurine is an Original Color Edition
     * @param revival
     *            whether the figurine is a Revival edition
     * @param plainCloth
     *            whether the figurine is a Plain Cloth version
     * @param broken
     *            whether the figurine is a Broken version
     * @param golden
     *            whether the figurine is a Golden Limited Edition
     * @param gold
     *            whether the figurine is a Gold version
     * @param _hk
     *            catalog value not used by this test. Column exist only to preserve
     *            the file format and are not part of the test logic
     * @param manga
     *            whether the figurine is a Manga edition
     * @param set
     *            whether the figurine represents a set
     * @param anniversary
     *            anniversary information in the format {@code year} or
     *            {@code year|type}
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @CsvFileSource(resources = "/figurines/MythClothDisplayableNames.csv", numLinesToSkip = 1)
    void shouldBuildExpectedDisplayName(String _mythClothOriginalName, String baseName, String displayName,
            String releaseJPY, String lineUp, String series, String group, String _metal, String oce, String revival,
            String plainCloth, String broken, String golden, String gold, String _hk, String manga, String set,
            String anniversary) {

        Figurine figurine = createFigurine(baseName, lineUp, series, group, releaseJPY, oce, revival, plainCloth,
                broken, golden, gold, manga, set, anniversary);
        String expectedName = FigurineDisplayNameBuilder.build(figurine);

        assertThat(displayName).isEqualTo(expectedName);
    }

    /**
     * Creates a minimal {@link Figurine} populated with the properties required by
     * {@link FigurineDisplayNameBuilder}.
     * <p>
     * If a release date is provided, it may be specified as either a complete date
     * ({@code M/d/yyyy}) or as a month and year ({@code M/yyyy}). When only the
     * month and year are provided, the first day of the month is assumed.
     *
     * @param baseName
     *            normalized figurine name
     * @param lineUpDescription
     *            line-up description
     * @param seriesDescription
     *            series description
     * @param groupDescription
     *            group description
     * @param releaseJPY
     *            Japanese release date
     * @param oce
     *            Original Color Edition flag
     * @param revival
     *            Revival edition flag
     * @param plainCloth
     *            Plain Cloth flag
     * @param broken
     *            Broken version flag
     * @param golden
     *            Golden Limited Edition flag
     * @param gold
     *            Gold version flag
     * @param manga
     *            Manga edition flag
     * @param set
     *            Set flag
     * @param anniversaryString
     *            anniversary information in the format {@code year} or
     *            {@code year|type}
     * @return a configured figurine for display name generation
     * @throws IllegalArgumentException
     *             if the release date is not in a supported format
     */
    private Figurine createFigurine(String baseName, String lineUpDescription, String seriesDescription,
            String groupDescription, String releaseJPY, String oce, String revival, String plainCloth, String broken,
            String golden, String gold, String manga, String set, String anniversaryString) {

        Figurine figurine = new Figurine();

        LineUp lineUp = new LineUp();
        lineUp.setDescription(lineUpDescription);

        Series series = new Series();
        series.setDescription(seriesDescription);

        Group group = new Group();
        group.setDescription(groupDescription);

        List<FigurineDistributor> distributors = null;
        if (releaseJPY != null && !releaseJPY.isEmpty()) {
            LocalDate localDate;
            try {
                localDate = LocalDate.parse(releaseJPY, FULL_DATE);
            } catch (DateTimeParseException ignored) {
                try {
                    localDate = YearMonth.parse(releaseJPY, MONTH_YEAR).atDay(1);
                } catch (DateTimeParseException ignored2) {
                    throw new IllegalArgumentException("Unsupported date format: " + releaseJPY);
                }
            }

            distributors = new ArrayList<>();

            FigurineDistributor distributor = new FigurineDistributor();
            distributor.setReleaseDate(localDate);
            distributors.add(distributor);
        }

        Anniversary anniversary = null;
        if (anniversaryString != null) {
            anniversary = new Anniversary();

            String[] ann = anniversaryString.split("\\|");
            anniversary.setYear(Integer.parseInt(ann[0]));
            if (ann.length > 1) {
                anniversary.setType(AnniversaryType.valueOf(ann[1]));
            }

        }

        figurine.setNormalizedName(baseName);
        figurine.setLineup(lineUp);
        figurine.setSeries(series);
        figurine.setGroup(group);
        figurine.setDistributors(distributors);
        figurine.setOce(toTrueOrFalse(oce));
        figurine.setRevival(toTrueOrFalse(revival));
        figurine.setPlainCloth(toTrueOrFalse(plainCloth));
        figurine.setBroken(toTrueOrFalse(broken));
        figurine.setGolden(toTrueOrFalse(golden));
        figurine.setGold(toTrueOrFalse(gold));
        figurine.setManga(toTrueOrFalse(manga));
        figurine.setSet(toTrueOrFalse(set));
        figurine.setAnniversary(anniversary);

        return figurine;
    }

    /**
     * Converts a string representation of a boolean into a {@link Boolean},
     * preserving {@code null} values.
     *
     * @param value
     *            the string to convert
     * @return {@code Boolean.TRUE}, {@code Boolean.FALSE}, or {@code null} if the
     *         supplied value is {@code null}
     */
    private Boolean toTrueOrFalse(String value) {
        if (value == null) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }
}
