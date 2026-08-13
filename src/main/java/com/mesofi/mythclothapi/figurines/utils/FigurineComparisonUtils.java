package com.mesofi.mythclothapi.figurines.utils;

import java.util.Objects;

import com.mesofi.mythclothapi.common.Descriptive;
import com.mesofi.mythclothapi.figurines.model.Figurine;

/**
 * Utility methods for comparing {@link Figurine} instances and determining
 * whether two figurines represent the same release characteristics.
 *
 * <p>
 * This class cannot be instantiated.
 * </p>
 */
public final class FigurineComparisonUtils {

    private FigurineComparisonUtils() {
    }

    /**
     * Determines whether a figurine represents a restock of another figurine.
     *
     * <p>
     * A figurine is considered a restock when both figurines are non-null, neither
     * is associated with an anniversary release, and both share the same
     * identifying characteristics.
     * </p>
     *
     * @param figurine
     *            the figurine to evaluate
     * @param other
     *            the figurine to compare against
     * @return {@code true} if the figurines have the same characteristics and
     *         neither is an anniversary release; {@code false} otherwise
     */
    public static boolean isRestock(Figurine figurine, Figurine other) {
        return figurine != null && other != null && figurine.getAnniversary() == null && other.getAnniversary() == null
                && hasSameCharacteristics(figurine, other);
    }

    /**
     * Determines whether two figurines have the same identifying characteristics.
     *
     * <p>
     * The comparison includes the normalized name, lineup, series, group, and the
     * attributes that distinguish different figurine releases, such as metal body,
     * OCE, revival, plain cloth, broken, golden, gold, manga, set, and articulable
     * characteristics.
     * </p>
     *
     * <p>
     * Descriptive properties are compared by their description rather than by
     * entity identity, allowing equivalent descriptive values to match even when
     * they are represented by different entity instances.
     * </p>
     *
     * @param figurine
     *            the first figurine to compare
     * @param other
     *            the second figurine to compare
     * @return {@code true} if both figurines have the same characteristics;
     *         {@code false} if either figurine is {@code null} or any
     *         characteristic differs
     */
    public static boolean hasSameCharacteristics(Figurine figurine, Figurine other) {
        if (figurine == null || other == null) {
            return false;
        }

        return Objects.equals(figurine.getNormalizedName(), other.getNormalizedName())
                && sameDescription(figurine.getLineup(), other.getLineup())
                && sameDescription(figurine.getSeries(), other.getSeries())
                && sameDescription(figurine.getGroup(), other.getGroup())
                && Objects.equals(figurine.getMetalBody(), other.getMetalBody())
                && Objects.equals(figurine.getOce(), other.getOce())
                && Objects.equals(figurine.getRevival(), other.getRevival())
                && Objects.equals(figurine.getPlainCloth(), other.getPlainCloth())
                && Objects.equals(figurine.getBroken(), other.getBroken())
                && Objects.equals(figurine.getGolden(), other.getGolden())
                && Objects.equals(figurine.getGold(), other.getGold())
                && Objects.equals(figurine.getManga(), other.getManga())
                && Objects.equals(figurine.getSet(), other.getSet())
                && Objects.equals(figurine.getArticulable(), other.getArticulable());
    }

    /**
     * Compares two {@link Descriptive} values by their descriptions.
     *
     * <p>
     * Both {@code null} values are considered equal. A {@code null} value and a
     * non-null value are considered different.
     * </p>
     *
     * @param d1
     *            the first descriptive value
     * @param d2
     *            the second descriptive value
     * @return {@code true} if both descriptions are equal; {@code false} otherwise
     */
    private static boolean sameDescription(Descriptive d1, Descriptive d2) {
        return Objects.equals(d1 == null ? null : d1.getDescription(), d2 == null ? null : d2.getDescription());
    }
}