package com.mesofi.mythclothapi.figurines;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FigurineSimilarityUtilsTest {

    @Test
    void calculateSimilarity_shouldReturnZeroWhenEitherStringIsNull() {
        assertThat(FigurineSimilarityUtils.calculateSimilarity(null, "Seiya")).isZero();
        assertThat(FigurineSimilarityUtils.calculateSimilarity("Seiya", null)).isZero();
    }

    @Test
    void calculateSimilarity_shouldReturnOneWhenStringsAreEqualOrBothEmpty() {
        assertThat(FigurineSimilarityUtils.calculateSimilarity("Seiya", "Seiya")).isEqualTo(1.0);
        assertThat(FigurineSimilarityUtils.calculateSimilarity("", "")).isEqualTo(1.0);
    }

    @Test
    void calculateSimilarity_shouldReturnZeroWhenOnlyOneStringIsEmpty() {
        assertThat(FigurineSimilarityUtils.calculateSimilarity("", "Seiya")).isZero();
        assertThat(FigurineSimilarityUtils.calculateSimilarity("Seiya", "")).isZero();
    }

    @Test
    void calculateSimilarity_shouldReturnExpectedScoreForOneEditDifference() {
        assertThat(FigurineSimilarityUtils.calculateSimilarity("Seiya", "Seiyaa")).isEqualTo(5d / 6d);
    }

    @Test
    void calculateSimilarity_shouldReturnLowerScoreForDifferentStrings() {
        assertThat(FigurineSimilarityUtils.calculateSimilarity("Seiya", "Hyoga")).isLessThan(0.3);
    }
}
