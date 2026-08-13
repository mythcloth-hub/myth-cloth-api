package com.mesofi.mythclothapi.figurines.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import com.mesofi.mythclothapi.catalogs.model.Group;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.figurines.model.Figurine;

class FigurineComparisonUtilsTest {

    @Test
    void isRestock_shouldReturnTrueWhenFigurinesMatchAndHaveNoAnniversary() {
        Figurine first = figurine("seiya", "Myth Cloth EX", "Classic", "Bronze", false, false, false, false, false,
                false, false, false, false);
        Figurine second = figurine("seiya", "Myth Cloth EX", "Classic", "Bronze", false, false, false, false, false,
                false, false, false, false);

        assertThat(FigurineComparisonUtils.isRestock(first, second)).isTrue();
    }

    @Test
    void isRestock_shouldReturnFalseWhenEitherFigurineIsNull() {
        Figurine figurine = figurine("seiya", "Myth Cloth EX", "Classic", "Bronze", false, false, false, false, false,
                false, false, false, false);

        assertThat(FigurineComparisonUtils.isRestock(null, figurine)).isFalse();
        assertThat(FigurineComparisonUtils.isRestock(figurine, null)).isFalse();
    }

    @Test
    void isRestock_shouldReturnFalseWhenEitherFigurineHasAnAnniversary() {
        Figurine first = figurine("seiya", "Myth Cloth EX", "Classic", "Bronze", false, false, false, false, false,
                false, false, false, false);
        Figurine second = figurine("seiya", "Myth Cloth EX", "Classic", "Bronze", false, false, false, false, false,
                false, false, false, false);
        second.setAnniversary(new com.mesofi.mythclothapi.anniversaries.model.Anniversary());

        assertThat(FigurineComparisonUtils.isRestock(first, second)).isFalse();
    }

    @Test
    void hasSameCharacteristics_shouldReturnTrueForEquivalentValues() {
        Figurine first = figurine("seiya", "Myth Cloth EX", "Classic", "Bronze", true, false, true, false, true, false,
                true, false, true);
        Figurine second = figurine("seiya", "Myth Cloth EX", "Classic", "Bronze", true, false, true, false, true, false,
                true, false, true);

        assertThat(FigurineComparisonUtils.hasSameCharacteristics(first, second)).isTrue();
    }

    @Test
    void hasSameCharacteristics_shouldReturnFalseWhenEitherFigurineIsNull() {
        Figurine figurine = figurine("seiya", "Myth Cloth EX", "Classic", "Bronze", false, false, false, false, false,
                false, false, false, false);

        assertThat(FigurineComparisonUtils.hasSameCharacteristics(null, figurine)).isFalse();
        assertThat(FigurineComparisonUtils.hasSameCharacteristics(figurine, null)).isFalse();
    }

    @Test
    void hasSameCharacteristics_shouldReturnFalseWhenNormalizedNameDiffers() {
        Figurine first = figurine("seiya", "Myth Cloth EX", "Classic", "Bronze", false, false, false, false, false,
                false, false, false, false);
        Figurine second = figurine("hyoga", "Myth Cloth EX", "Classic", "Bronze", false, false, false, false, false,
                false, false, false, false);

        assertThat(FigurineComparisonUtils.hasSameCharacteristics(first, second)).isFalse();
    }

    @Test
    void hasSameCharacteristics_shouldReturnFalseWhenLineupDescriptionDiffers() {
        Figurine first = figurine("seiya", "Myth Cloth EX", "Classic", "Bronze", false, false, false, false, false,
                false, false, false, false);
        Figurine second = figurine("seiya", "Myth Cloth", "Classic", "Bronze", false, false, false, false, false, false,
                false, false, false);

        assertThat(FigurineComparisonUtils.hasSameCharacteristics(first, second)).isFalse();
    }

    @Test
    void hasSameCharacteristics_shouldReturnFalseWhenSeriesDescriptionDiffers() {
        Figurine first = figurine("seiya", "Myth Cloth EX", "Classic", "Bronze", false, false, false, false, false,
                false, false, false, false);
        Figurine second = figurine("seiya", "Myth Cloth EX", "Eternal", "Bronze", false, false, false, false, false,
                false, false, false, false);

        assertThat(FigurineComparisonUtils.hasSameCharacteristics(first, second)).isFalse();
    }

    @Test
    void hasSameCharacteristics_shouldReturnFalseWhenGroupDescriptionDiffers() {
        Figurine first = figurine("seiya", "Myth Cloth EX", "Classic", "Bronze", false, false, false, false, false,
                false, false, false, false);
        Figurine second = figurine("seiya", "Myth Cloth EX", "Classic", "Silver", false, false, false, false, false,
                false, false, false, false);

        assertThat(FigurineComparisonUtils.hasSameCharacteristics(first, second)).isFalse();
    }

    @Test
    void hasSameCharacteristics_shouldReturnFalseWhenFirstDescriptiveValueIsNull() {
        Figurine first = figurine("seiya", null, "Classic", "Bronze", false, false, false, false, false, false, false,
                false, false);
        Figurine second = figurine("seiya", "Myth Cloth EX", "Classic", "Bronze", false, false, false, false, false,
                false, false, false, false);

        assertThat(FigurineComparisonUtils.hasSameCharacteristics(first, second)).isFalse();
    }

    @Test
    void hasSameCharacteristics_shouldReturnFalseWhenSecondDescriptiveValueIsNull() {
        Figurine first = figurine("seiya", "Myth Cloth EX", "Classic", "Bronze", false, false, false, false, false,
                false, false, false, false);
        Figurine second = figurine("seiya", null, "Classic", "Bronze", false, false, false, false, false, false, false,
                false, false);

        assertThat(FigurineComparisonUtils.hasSameCharacteristics(first, second)).isFalse();
    }

    @Test
    void sameDescription_shouldHandleNullAndNonNullValues() throws Exception {
        Method method = FigurineComparisonUtils.class.getDeclaredMethod("sameDescription",
                com.mesofi.mythclothapi.common.Descriptive.class, com.mesofi.mythclothapi.common.Descriptive.class);
        method.setAccessible(true);

        assertThat((Boolean) method.invoke(null, null, null)).isTrue();
        assertThat((Boolean) method.invoke(null, descriptiveLineUp(null), descriptiveLineUp("Myth Cloth EX")))
                .isFalse();
        assertThat((Boolean) method.invoke(null, descriptiveLineUp("Myth Cloth EX"), descriptiveLineUp(null)))
                .isFalse();
    }

    @Test
    void hasSameCharacteristics_shouldReturnFalseWhenBooleanFlagsDiffer() {
        Figurine first = figurine("seiya", "Myth Cloth EX", "Classic", "Bronze", false, false, false, false, false,
                false, false, false, false);
        Figurine second = figurine("seiya", "Myth Cloth EX", "Classic", "Bronze", true, false, false, false, false,
                false, false, false, false);

        assertThat(FigurineComparisonUtils.hasSameCharacteristics(first, second)).isFalse();
    }

    @Test
    void hasSameCharacteristics_shouldReturnFalseWhenLastBooleanFlagDiffers() {
        Figurine first = figurine("seiya", "Myth Cloth EX", "Classic", "Bronze", false, false, false, false, false,
                false, false, false, false);
        Figurine second = figurine("seiya", "Myth Cloth EX", "Classic", "Bronze", false, false, false, false, false,
                false, false, false, false, true);

        assertThat(FigurineComparisonUtils.hasSameCharacteristics(first, second)).isFalse();
    }

    private Figurine figurine(String normalizedName, String lineupDescription, String seriesDescription,
            String groupDescription, Boolean... flags) {
        Figurine figurine = new Figurine();
        figurine.setNormalizedName(normalizedName);
        figurine.setLineup(descriptiveLineUp(lineupDescription));
        figurine.setSeries(descriptiveSeries(seriesDescription));
        figurine.setGroup(descriptiveGroup(groupDescription));
        figurine.setMetalBody(flag(flags, 0));
        figurine.setOce(flag(flags, 1));
        figurine.setRevival(flag(flags, 2));
        figurine.setPlainCloth(flag(flags, 3));
        figurine.setBroken(flag(flags, 4));
        figurine.setGolden(flag(flags, 5));
        figurine.setGold(flag(flags, 6));
        figurine.setManga(flag(flags, 7));
        figurine.setSet(flag(flags, 8));
        figurine.setArticulable(flag(flags, 9) != null ? flag(flags, 9) : flag(flags, 8));
        return figurine;
    }

    private Boolean flag(Boolean[] flags, int index) {
        return index < flags.length ? flags[index] : null;
    }

    private LineUp descriptiveLineUp(String description) {
        LineUp lineUp = new LineUp();
        lineUp.setDescription(description);
        return lineUp;
    }

    private Series descriptiveSeries(String description) {
        Series series = new Series();
        series.setDescription(description);
        return series;
    }

    private Group descriptiveGroup(String description) {
        Group group = new Group();
        group.setDescription(description);
        return group;
    }
}
