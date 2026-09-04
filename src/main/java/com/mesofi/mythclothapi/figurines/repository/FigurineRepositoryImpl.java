package com.mesofi.mythclothapi.figurines.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.figurines.model.Figurine;

/**
 * Custom repository implementation for executing advanced figurine queries.
 *
 * <p>
 * This repository uses native SQL queries through {@link EntityManager} to
 * support dynamic filtering, custom sorting, and pagination requirements that
 * are not easily handled by Spring Data derived queries.
 * </p>
 *
 * <p>
 * The repository supports:
 * </p>
 * <ul>
 * <li>Dynamic filtering based on {@link FigurineFilter}.</li>
 * <li>Pagination through Spring Data {@link Pageable}.</li>
 * <li>Custom ordering based on figurine release status and dates.</li>
 * <li>Counting total and collectable figurines.</li>
 * <li>Filtering figurines by release year.</li>
 * </ul>
 *
 * <p>
 * Figurines are categorized by their calculated release status:
 * </p>
 * <ul>
 * <li>{@code RELEASED}: Figurines with a release date in the past or
 * present.</li>
 * <li>{@code ANNOUNCED}: Figurines with a future release date.</li>
 * <li>{@code PROTOTYPE}: Figurines with an announcement date but no release
 * date.</li>
 * <li>{@code RUMORED}: Figurines without announcement or release
 * information.</li>
 * </ul>
 *
 * @see FigurineQueryRepository
 * @see FigurineFilter
 */
@Repository
public class FigurineRepositoryImpl implements FigurineQueryRepository {

    @PersistenceContext
    private EntityManager em;

    /**
     * Base SQL query used as the starting point for dynamic figurine queries.
     *
     * <p>
     * The query calculates the release status for each figurine and retrieves the
     * first distributor associated with each figurine. Additional filtering,
     * sorting, and pagination clauses are appended dynamically.
     * </p>
     */
    private final String BASE_SQL = """
            SELECT
                f.*
            FROM figurines f
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

    /**
     * Retrieves a paginated list of figurines matching the specified filter.
     *
     * <p>
     * In addition to the requested page of figurines, the returned page contains
     * the total number of matching figurines and the total number of collectable
     * figurines.
     * </p>
     *
     * <p>
     * A figurine is considered collectable when its calculated release status is
     * {@code ANNOUNCED} or {@code RELEASED}.
     * </p>
     *
     * @param filter
     *            the filtering criteria used to restrict the search results
     * @param pageable
     *            the pagination information, including page size and offset
     * @return a paginated result containing the matching figurines and collectable
     *         figurine count
     */
    @Override
    public CollectablePageImpl<Figurine> findPaginated(FigurineFilter filter, Pageable pageable) {
        SearchQueryContext queryContext = getSearchQueryContext(filter);

        StringBuilder sqlBuilder = queryContext.sql();
        Map<String, Object> params = queryContext.params();

        List<Figurine> result = executeAndGetContent("%s %s".formatted(sqlBuilder, buildOrderByStatement()), params,
                pageable);

        long totalFigurines = executeAndGetTotal(buildCountStatement().formatted(sqlBuilder), params);

        long totalCollectableFigurines = executeAndGetTotal(
                buildCountStatement().formatted("%s %s".formatted(sqlBuilder, buildCollectablePredicate())), params);

        return new CollectablePageImpl<>(result, pageable, totalFigurines, totalCollectableFigurines);
    }

    /**
     * Retrieves all figurines matching the specified filter criteria.
     *
     * <p>
     * The results are ordered according to the repository's default release status
     * ordering.
     * </p>
     *
     * @param filter
     *            the filtering criteria used to restrict the results
     * @return a list of matching figurines
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Figurine> findAll(FigurineFilter filter) {
        SearchQueryContext queryContext = getSearchQueryContext(filter);

        StringBuilder sql = queryContext.sql();
        sql.append(" ").append(buildOrderByStatement());

        Query query = em.createNativeQuery(sql.toString(), Figurine.class);
        queryContext.params().forEach(query::setParameter);

        return query.getResultList();
    }

    /**
     * Retrieves all figurines released during the specified year.
     *
     * <p>
     * The query filters figurines by the year extracted from their release date and
     * applies the default release status ordering.
     * </p>
     *
     * @param year
     *            the release year used to filter figurines
     * @return a list of figurines released during the specified year
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Figurine> findAllByYear(int year) {
        SearchQueryContext queryContext = getSearchQueryContext(null);

        StringBuilder sql = queryContext.sql();
        Map<String, Object> params = queryContext.params();

        sql.append(" AND EXTRACT(YEAR FROM fd.release_date) = :year");
        sql.append(" ").append(buildOrderByStatement());
        params.put("year", year);

        Query query = em.createNativeQuery(sql.toString(), Figurine.class);
        queryContext.params().forEach(query::setParameter);

        return query.getResultList();
    }

    /**
     * Builds the SQL query and parameter map based on the supplied filter.
     *
     * <p>
     * Only predicates corresponding to non-null or non-empty filter values are
     * appended to the base query.
     * </p>
     *
     * @param filter
     *            the filter criteria used to build the dynamic query; may be
     *            {@code null}
     * @return a {@link SearchQueryContext} containing the generated SQL and its
     *         named parameters
     */
    private SearchQueryContext getSearchQueryContext(FigurineFilter filter) {
        StringBuilder dynamicSql = new StringBuilder(BASE_SQL);
        Map<String, Object> params = new HashMap<>();

        if (Objects.isNull(filter)) {
            return new SearchQueryContext(dynamicSql, params);
        }

        // Dynamic filters
        if (Objects.nonNull(filter.figurineIds()) && !filter.figurineIds().isEmpty()) {
            dynamicSql.append(" AND f.id IN (:figurineIds)");
            params.put("figurineIds", filter.figurineIds());
        }
        if (StringUtils.hasLength(filter.name())) {
            dynamicSql.append(" AND LOWER(normalized_name) LIKE LOWER(:name)");
            params.put("name", "%" + filter.name() + "%");
        }
        if (Objects.nonNull(filter.lineUpId())) {
            dynamicSql.append(" AND lineup_id = :lineUpId");
            params.put("lineUpId", filter.lineUpId());
        }
        if (Objects.nonNull(filter.seriesId())) {
            dynamicSql.append(" AND series_id = :seriesId");
            params.put("seriesId", filter.seriesId());
        }
        if (Objects.nonNull(filter.groupIds()) && !filter.groupIds().isEmpty()) {
            dynamicSql.append(" AND group_id IN (:groupIds)");
            params.put("groupIds", filter.groupIds());
        }
        if (Objects.nonNull(filter.distributionId())) {
            dynamicSql.append(" AND distribution_id = :distributionId");
            params.put("distributionId", filter.distributionId());
        }
        if (Objects.nonNull(filter.anniversaryId())) {
            dynamicSql.append(" AND anniversary_id = :anniversaryId");
            params.put("anniversaryId", filter.anniversaryId());
        }
        if (Objects.nonNull(filter.metalBody())) {
            dynamicSql.append(" AND is_metal_body = :metalBody");
            params.put("metalBody", filter.metalBody());
        }
        if (Objects.nonNull(filter.oce())) {
            dynamicSql.append(" AND is_oce = :oce");
            params.put("oce", filter.oce());
        }
        if (Objects.nonNull(filter.revival())) {
            dynamicSql.append(" AND is_revival = :revival");
            params.put("revival", filter.revival());
        }
        if (Objects.nonNull(filter.plainCloth())) {
            dynamicSql.append(" AND is_plain_cloth = :plainCloth");
            params.put("plainCloth", filter.plainCloth());
        }
        if (Objects.nonNull(filter.broken())) {
            dynamicSql.append(" AND is_broken = :broken");
            params.put("broken", filter.broken());
        }
        if (Objects.nonNull(filter.golden())) {
            dynamicSql.append(" AND is_golden = :golden");
            params.put("golden", filter.golden());
        }
        if (Objects.nonNull(filter.gold())) {
            dynamicSql.append(" AND is_gold = :gold");
            params.put("gold", filter.gold());
        }
        if (Objects.nonNull(filter.manga())) {
            dynamicSql.append(" AND is_manga = :manga");
            params.put("manga", filter.manga());
        }
        if (Objects.nonNull(filter.set())) {
            dynamicSql.append(" AND is_set = :set");
            params.put("set", filter.set());
        }
        if (Objects.nonNull(filter.articulable())) {
            dynamicSql.append(" AND is_articulable = :articulable");
            params.put("articulable", filter.articulable());
        }
        if (Objects.nonNull(filter.releaseStatuses()) && !filter.releaseStatuses().isEmpty()) {
            dynamicSql.append(" AND current_release_status IN (:status)");
            params.put("status", filter.releaseStatuses());
        }
        if (Objects.nonNull(filter.restocks())) {
            if (filter.restocks()) {
                dynamicSql.append(" AND previous_release_id IS NOT NULL");
            } else {
                dynamicSql.append(" AND previous_release_id IS NULL");
            }
        }

        return new SearchQueryContext(dynamicSql, params);
    }

    /**
     * Builds the SQL {@code ORDER BY} clause used to sort figurines by release
     * priority and relevant dates.
     *
     * <p>
     * The ordering prioritizes announced and released figurines before prototypes
     * and rumored items. Within each status, the appropriate release, announcement,
     * or creation date is used to determine the order.
     * </p>
     *
     * @return the SQL {@code ORDER BY} clause
     */
    private String buildOrderByStatement() {
        return """
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
    }

    /**
     * Builds the SQL predicate used to identify collectable figurines.
     *
     * <p>
     * A figurine is considered collectable when its calculated release status is
     * either {@code ANNOUNCED} or {@code RELEASED}.
     * </p>
     *
     * @return the SQL predicate for collectable figurines
     */
    private String buildCollectablePredicate() {
        return "AND current_release_status IN ('ANNOUNCED', 'RELEASED')";
    }

    /**
     * Creates a SQL count query template for a generated query.
     *
     * <p>
     * The generated query is used as a subquery so that the total number of
     * matching records can be calculated independently of pagination.
     * </p>
     *
     * @return a SQL count query template containing a placeholder for the generated
     *         query
     */
    private String buildCountStatement() {
        return "SELECT COUNT(*) FROM (%s) count_q";
    }

    /**
     * Executes a paginated native SQL query and maps the results to
     * {@link Figurine} entities.
     *
     * @param sql
     *            the SQL query to execute
     * @param params
     *            the named parameters and their values
     * @param pageable
     *            the pagination configuration
     * @return the figurines contained in the requested page
     */
    @SuppressWarnings("unchecked")
    private List<Figurine> executeAndGetContent(String sql, Map<String, Object> params, Pageable pageable) {

        Query query = em.createNativeQuery(sql, Figurine.class);
        params.forEach(query::setParameter);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return query.getResultList();
    }

    /**
     * Executes a native SQL count query.
     *
     * @param sql
     *            the SQL count query to execute
     * @param params
     *            the named parameters and their values
     * @return the total number of matching records
     */
    private long executeAndGetTotal(String sql, Map<String, Object> params) {
        Query query = em.createNativeQuery(sql);
        params.forEach(query::setParameter);

        return ((Number) query.getSingleResult()).longValue();
    }
}
