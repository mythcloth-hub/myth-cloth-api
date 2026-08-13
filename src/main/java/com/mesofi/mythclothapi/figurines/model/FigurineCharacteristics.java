package com.mesofi.mythclothapi.figurines.model;

import com.mesofi.mythclothapi.common.Descriptive;

/**
 * Represents the characteristics used to describe and compare a figurine.
 *
 * <p>
 * The characteristics include the figurine's normalized name, catalog
 * classifications, and physical or release-related attributes. Descriptive
 * entities such as lineup, series, and group are represented by their
 * descriptions.
 * </p>
 *
 * @param normalizedName
 *            the normalized name of the figurine
 * @param lineup
 *            the figurine's lineup description
 * @param series
 *            the figurine's series description
 * @param group
 *            the figurine's group description
 * @param metalBody
 *            whether the figurine has a metal body
 * @param oce
 *            whether the figurine is an Original Color Edition
 * @param revival
 *            whether the figurine is a revival release
 * @param plainCloth
 *            whether the figurine includes plain cloth
 * @param broken
 *            whether the figurine represents a broken version
 * @param golden
 *            whether the figurine has a golden variant
 * @param gold
 *            whether the figurine has a gold-related variant
 * @param manga
 *            whether the figurine is based on a manga version
 * @param set
 *            whether the figurine is part of a set
 * @param articulable
 *            whether the figurine is articulable
 */
public record FigurineCharacteristics(String normalizedName, String lineup, String series, String group,
        Boolean metalBody, Boolean oce, Boolean revival, Boolean plainCloth, Boolean broken, Boolean golden,
        Boolean gold, Boolean manga, Boolean set, Boolean articulable) {

    /**
     * Creates a {@link FigurineCharacteristics} instance from the specified
     * figurine.
     *
     * <p>
     * Descriptive properties are converted to their description values while
     * preserving {@code null} values when the corresponding entity is absent.
     * </p>
     *
     * @param figurine
     *            the figurine from which the characteristics are extracted
     * @return the characteristics extracted from the specified figurine
     */
    public static FigurineCharacteristics from(Figurine figurine) {
        return new FigurineCharacteristics(figurine.getNormalizedName(), description(figurine.getLineup()),
                description(figurine.getSeries()), description(figurine.getGroup()), figurine.getMetalBody(),
                figurine.getOce(), figurine.getRevival(), figurine.getPlainCloth(), figurine.getBroken(),
                figurine.getGolden(), figurine.getGold(), figurine.getManga(), figurine.getSet(),
                figurine.getArticulable());
    }

    /**
     * Returns the description of the specified descriptive entity.
     *
     * @param descriptive
     *            the descriptive entity; may be {@code null}
     * @return the entity description, or {@code null} when the entity is
     *         {@code null}
     */
    private static String description(Descriptive descriptive) {
        return descriptive == null ? null : descriptive.getDescription();
    }
}