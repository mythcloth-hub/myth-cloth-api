package com.mesofi.mythclothapi.figurines;

import org.apache.commons.text.similarity.LevenshteinDistance;

public class FigurineSimilarityUtils {

    private static final LevenshteinDistance LEVENSHTEIN = new LevenshteinDistance();

    /**
     * Calculates percentage similarity between two strings.
     * 
     * @return double between 0.0 (completely different) and 1.0 (exact match)
     */
    public static double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        if (s1.equals(s2)) {
            return 1.0;
        }

        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) {
            return 1.0; // Both strings are empty
        }

        int distance = LEVENSHTEIN.apply(s1, s2);

        return 1.0 - ((double) distance / maxLength);
    }
}
