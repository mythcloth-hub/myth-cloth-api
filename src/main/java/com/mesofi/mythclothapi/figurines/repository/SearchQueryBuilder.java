package com.mesofi.mythclothapi.figurines.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.util.StringUtils;

import com.mesofi.mythclothapi.figurines.FigurineFilter;

public class SearchQueryBuilder {

    public static final String BASE_FIGURINE_SEARCH = """
            SELECT
                %s
            FROM figurines f
            %s
            LEFT JOIN (
                SELECT *
                FROM (
                    SELECT fd.*,
                        ROW_NUMBER() OVER (PARTITION BY figurine_id ORDER BY id) rn
                    FROM figurine_distributor fd
                ) x
                WHERE rn = 1
            ) fd ON fd.figurine_id = f.id
            WHERE 1 = 1
            """;

    public static final String FIGURINE_SEARCH_ORDER_BY = """
            ORDER BY
                CASE current_release_status
                    WHEN 'ANNOUNCED'  THEN 1
                    WHEN 'RELEASED'   THEN 2
                    WHEN 'PROTOTYPE'  THEN 3
                    WHEN 'UNRELEASED' THEN 4
                    WHEN 'RUMORED'    THEN 5
                END,
                CASE
                    WHEN current_release_status IN ('ANNOUNCED', 'RELEASED') THEN release_date
                END DESC,
                CASE
                    WHEN current_release_status IN ('ANNOUNCED', 'RELEASED') THEN f.id
                END,
                CASE
                    WHEN current_release_status = 'PROTOTYPE' THEN announcement_date
                END DESC,
                CASE
                    WHEN current_release_status = 'PROTOTYPE' THEN f.id
                END,
                CASE
                    WHEN current_release_status = 'UNRELEASED' THEN announcement_date
                END DESC,
                CASE
                    WHEN current_release_status = 'UNRELEASED' THEN f.id
                END,
                CASE
                    WHEN current_release_status = 'RUMORED' THEN f.creation_date
                END,
                CASE
                    WHEN current_release_status = 'RUMORED' THEN f.id
                END
            """;

    public static final String COUNT_QUERY = """
            SELECT
                COUNT(*)
            FROM (%s) count_q
            """;

    public static final String RELEASED_OR_ANNOUNCED_FILTER = """
            AND current_release_status IN ('ANNOUNCED', 'RELEASED')
            """;

    public static SearchQueryContext buildFigurineSearchQueryContext(FigurineFilter filter, Long collectionId) {
        String selectClause = """
                f.id, f.normalized_name, f.display_name, f.current_release_status,
                lu.description AS lineup_description, g.description AS group_description, a.name as anniversary_description,
                f.is_metal_body, f.is_oce, f.is_revival, f.is_gold, oi.official_images AS image_url
                """;
        String joinClause = """
                LEFT JOIN lineups lu ON lu.id = f.lineup_id
                LEFT JOIN groups g ON g.id = f.group_id
                LEFT JOIN anniversaries a ON a.id = f.anniversary_id
                LEFT JOIN (
                    SELECT DISTINCT ON (figurine_id)
                        figurine_id,
                        official_images
                    FROM official_images
                    ORDER BY figurine_id
                ) oi ON oi.figurine_id = f.id
                """;

        StringBuilder baseQuery = buildBaseSearchQuery(selectClause, joinClause);
        return createSearchQueryContext(baseQuery, filter, collectionId);
    }

    public static SearchQueryContext buildFigurineSearchQueryContext() {
        String selectClause = "f.*, NULL as collection_figurine_id";
        String joinClause = "";

        StringBuilder baseQuery = buildBaseSearchQuery(selectClause, joinClause);
        return createSearchQueryContext(baseQuery, null, null);
    }

    private static SearchQueryContext createSearchQueryContext(StringBuilder baseQuery, FigurineFilter filter,
            Long collectionId) {
        Map<String, Object> params = new HashMap<>();

        if (Objects.isNull(filter)) {
            return new SearchQueryContext(baseQuery, params);
        }

        // Dynamic filters
        if (Objects.nonNull(collectionId)) {
            baseQuery.append(" AND ccf.collection_id = :collectionId");
            params.put("collectionId", collectionId);
        }
        if (Objects.nonNull(filter.figurineIds()) && !filter.figurineIds().isEmpty()) {
            baseQuery.append(" AND f.id IN (:figurineIds)");
            params.put("figurineIds", filter.figurineIds());
        }
        if (StringUtils.hasLength(filter.name())) {
            baseQuery.append(" AND LOWER(normalized_name) LIKE LOWER(:name)");
            params.put("name", "%" + filter.name() + "%");
        }
        if (Objects.nonNull(filter.lineUpId())) {
            baseQuery.append(" AND lineup_id = :lineUpId");
            params.put("lineUpId", filter.lineUpId());
        }
        if (Objects.nonNull(filter.seriesId())) {
            baseQuery.append(" AND series_id = :seriesId");
            params.put("seriesId", filter.seriesId());
        }
        if (Objects.nonNull(filter.groupIds()) && !filter.groupIds().isEmpty()) {
            baseQuery.append(" AND group_id IN (:groupIds)");
            params.put("groupIds", filter.groupIds());
        }
        if (Objects.nonNull(filter.distributionId())) {
            baseQuery.append(" AND distribution_id = :distributionId");
            params.put("distributionId", filter.distributionId());
        }
        if (Objects.nonNull(filter.anniversaryId())) {
            baseQuery.append(" AND anniversary_id = :anniversaryId");
            params.put("anniversaryId", filter.anniversaryId());
        }
        if (Objects.nonNull(filter.metalBody())) {
            baseQuery.append(" AND is_metal_body = :metalBody");
            params.put("metalBody", filter.metalBody());
        }
        if (Objects.nonNull(filter.oce())) {
            baseQuery.append(" AND is_oce = :oce");
            params.put("oce", filter.oce());
        }
        if (Objects.nonNull(filter.revival())) {
            baseQuery.append(" AND is_revival = :revival");
            params.put("revival", filter.revival());
        }
        if (Objects.nonNull(filter.plainCloth())) {
            baseQuery.append(" AND is_plain_cloth = :plainCloth");
            params.put("plainCloth", filter.plainCloth());
        }
        if (Objects.nonNull(filter.broken())) {
            baseQuery.append(" AND is_broken = :broken");
            params.put("broken", filter.broken());
        }
        if (Objects.nonNull(filter.golden())) {
            baseQuery.append(" AND is_golden = :golden");
            params.put("golden", filter.golden());
        }
        if (Objects.nonNull(filter.gold())) {
            baseQuery.append(" AND is_gold = :gold");
            params.put("gold", filter.gold());
        }
        if (Objects.nonNull(filter.manga())) {
            baseQuery.append(" AND is_manga = :manga");
            params.put("manga", filter.manga());
        }
        if (Objects.nonNull(filter.set())) {
            baseQuery.append(" AND is_set = :set");
            params.put("set", filter.set());
        }
        if (Objects.nonNull(filter.articulable())) {
            baseQuery.append(" AND is_articulable = :articulable");
            params.put("articulable", filter.articulable());
        }
        if (Objects.nonNull(filter.releaseStatuses()) && !filter.releaseStatuses().isEmpty()) {
            baseQuery.append(" AND current_release_status IN (:status)");
            params.put("status", filter.releaseStatuses());
        }
        if (Objects.nonNull(filter.restocks())) {
            if (filter.restocks()) {
                baseQuery.append(" AND previous_release_id IS NOT NULL");
            } else {
                baseQuery.append(" AND previous_release_id IS NULL");
            }
        }

        return new SearchQueryContext(baseQuery, params);
    }

    private static StringBuilder buildBaseSearchQuery(String selectClause) {
        return new StringBuilder(String.format(BASE_FIGURINE_SEARCH, selectClause, ""));
    }
    private static StringBuilder buildBaseSearchQuery(String selectClause, String joinClause) {
        return new StringBuilder(String.format(BASE_FIGURINE_SEARCH, selectClause, joinClause));
    }
}
