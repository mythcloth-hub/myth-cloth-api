package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.ANNOUNCED;
import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.RELEASED;

import java.util.List;

/**
 * Factory utility for constructing {@link FigurineFilter} instances from
 * request parameters.
 *
 * <p>
 * This class centralizes the logic for building figurine filters, including
 * name normalization and conversion of a single release status into a list of
 * release statuses.
 */
public final class FigurineFilterFactory {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private FigurineFilterFactory() {
    }

    /**
     * Constructs a {@link FigurineFilter} for a single release status.
     *
     * <p>
     * This is a convenience overload that delegates to
     * {@link #build(List, String, Long, Long, List, Long, Long, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, List, Boolean)}
     * by converting the provided release status into a single-element list.
     *
     * @param figurineIds
     *            optional figurine identifier list for filtering by collector's
     *            figurines
     * @param name
     *            optional name filter
     * @param lineUpId
     *            optional lineup identifier
     * @param seriesId
     *            optional series identifier
     * @param groupId
     *            optional group identifier
     * @param distributionId
     *            optional distribution identifier
     * @param anniversaryId
     *            optional anniversary identifier
     * @param metalBody
     *            optional metal body flag
     * @param oce
     *            optional OCE flag
     * @param revival
     *            optional revival flag
     * @param plainCloth
     *            optional plain cloth flag
     * @param broken
     *            optional broken flag
     * @param golden
     *            optional golden flag
     * @param gold
     *            optional gold flag
     * @param manga
     *            optional manga flag
     * @param set
     *            optional set flag
     * @param articulable
     *            optional articulable flag
     * @param releaseStatus
     *            optional release status filter
     * @param restocks
     *            optional flag indicating whether to filter restocks
     * @return a new {@link FigurineFilter} instance
     */
    public static FigurineFilter build(List<Long> figurineIds, String name, Long lineUpId, Long seriesId, Long groupId,
            Long distributionId, Long anniversaryId, Boolean metalBody, Boolean oce, Boolean revival,
            Boolean plainCloth, Boolean broken, Boolean golden, Boolean gold, Boolean manga, Boolean set,
            Boolean articulable, String releaseStatus, Boolean restocks) {

        return build(figurineIds, name, lineUpId, seriesId, groupId == null ? null : List.of(groupId), distributionId,
                anniversaryId, metalBody, oce, revival, plainCloth, broken, golden, gold, manga, set, articulable,
                releaseStatus == null ? null : List.of(releaseStatus), restocks);
    }

    /**
     * Constructs a {@link FigurineFilter} from the provided request parameters.
     *
     * <p>
     * The name parameter is trimmed and must contain at least three characters. If
     * the name is {@code null} or contains fewer than three characters after
     * trimming, an empty string is used instead.
     *
     * @param figurineIds
     *            optional figurine identifier list for filtering by collector's
     *            figurines
     * @param name
     *            optional name filter
     * @param lineUpId
     *            optional lineup identifier
     * @param seriesId
     *            optional series identifier
     * @param groupIds
     *            optional group identifier list
     * @param distributionId
     *            optional distribution identifier
     * @param anniversaryId
     *            optional anniversary identifier
     * @param metalBody
     *            optional metal body flag
     * @param oce
     *            optional OCE flag
     * @param revival
     *            optional revival flag
     * @param plainCloth
     *            optional plain cloth flag
     * @param broken
     *            optional broken flag
     * @param golden
     *            optional golden flag
     * @param gold
     *            optional gold flag
     * @param manga
     *            optional manga flag
     * @param set
     *            optional set flag
     * @param articulable
     *            optional articulable flag
     * @param releaseStatuses
     *            optional release status filters
     * @param restocks
     *            optional flag indicating whether to filter restocks
     * @return a new {@link FigurineFilter} instance
     */
    public static FigurineFilter build(List<Long> figurineIds, String name, Long lineUpId, Long seriesId,
            List<Long> groupIds, Long distributionId, Long anniversaryId, Boolean metalBody, Boolean oce,
            Boolean revival, Boolean plainCloth, Boolean broken, Boolean golden, Boolean gold, Boolean manga,
            Boolean set, Boolean articulable, List<String> releaseStatuses, Boolean restocks) {

        String figurineName = normalizeName(name);

        return new FigurineFilter(figurineIds, figurineName, lineUpId, seriesId, groupIds, distributionId,
                anniversaryId, metalBody, oce, revival, plainCloth, broken, golden, gold, manga, set, articulable,
                releaseStatuses, restocks);
    }

    public static FigurineFilter buildReleasedAndAnnounced() {
        return build(List.of(), null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, List.of(RELEASED.name(), ANNOUNCED.name()), null);
    }

    public static FigurineFilter buildReleasedAndAnnounced(boolean restocks) {
        return build(List.of(), null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, List.of(RELEASED.name(), ANNOUNCED.name()), restocks);
    }

    public static FigurineFilter buildReleasedAnnouncedAndGroups(List<Long> groupIds) {
        return build(List.of(), null, null, null, groupIds, null, null, null, null, null, null, null, null, null, null,
                null, null, List.of(RELEASED.name(), ANNOUNCED.name()), null);
    }

    /**
     * Normalizes a figurine name for filtering.
     *
     * <p>
     * The name is trimmed before validation. Names containing fewer than three
     * characters after trimming are treated as empty filters.
     *
     * @param name
     *            the name to normalize
     * @return the trimmed name when it contains at least three characters;
     *         otherwise an empty string
     */
    private static String normalizeName(String name) {
        if (name == null) {
            return "";
        }

        String trimmed = name.trim();
        return trimmed.length() >= 3 ? trimmed : "";
    }

}
