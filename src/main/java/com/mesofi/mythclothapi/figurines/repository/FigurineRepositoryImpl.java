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
 * Custom repository implementation responsible for executing advanced figurine queries.
 *
 * <p>This repository uses native SQL queries through {@link EntityManager} to support dynamic
 * filtering, custom sorting, and pagination requirements that are not easily handled through Spring
 * Data derived queries.
 *
 * <p>The repository provides:
 *
 * <ul>
 *   <li>Dynamic filtering based on {@link FigurineFilter}.
 *   <li>Pagination support through Spring Data {@link Pageable}.
 *   <li>Custom ordering based on figurine release status.
 *   <li>Calculation of total figurines and total collectable figurines.
 *   <li>Queries optimized to retrieve the first distributor information associated with each
 *       figurine.
 * </ul>
 *
 * <p>Figurines are categorized into release statuses:
 *
 * <ul>
 *   <li>{@code RELEASED}: Figurines with a release date in the past or present.
 *   <li>{@code ANNOUNCED}: Figurines with a future release date.
 *   <li>{@code PROTOTYPE}: Figurines with an announcement date but no release date.
 *   <li>{@code RUMORED}: Figurines without announcement or release information.
 * </ul>
 *
 * @see FigurineRepositoryCustom
 * @see FigurineFilter
 */
@Repository
public class FigurineRepositoryImpl implements FigurineRepositoryCustom {

  @PersistenceContext private EntityManager em;

  private final String BASE_SQL =
      """
	                SELECT
	                    t.*
	                FROM (
	                    SELECT
	                        CASE
	                            WHEN fd.release_date IS NULL AND fd.announcement_date IS NULL THEN 'RUMORED'
	                            WHEN fd.release_date IS NULL AND fd.announcement_date IS NOT NULL THEN 'PROTOTYPE'
	                            WHEN fd.release_date IS NOT NULL AND fd.release_date > CURRENT_DATE THEN 'ANNOUNCED'
	                            ELSE 'RELEASED'
	                        END AS release_status,
	                        fd.announcement_date,
	                        fd.release_date,
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
	                ) t
	                WHERE 1 = 1
	                """;

  /**
   * Retrieves a paginated list of figurines using the provided search criteria.
   *
   * <p>The result includes pagination metadata and the total number of collectable figurines.
   * Collectable figurines are those with a release status of {@code ANNOUNCED} or {@code RELEASED}.
   *
   * @param filter filtering criteria used to restrict the search results
   * @param pageable pagination information including page size and offset
   * @return a paginated result containing figurines and collectable count information
   */
  public CollectablePageImpl<Figurine> findPaginated(FigurineFilter filter, Pageable pageable) {
    SearchQueryContext queryContext = getSearchQueryContext(filter);

    StringBuilder sqlBuilder = queryContext.sql();
    Map<String, Object> params = queryContext.params();

    List<Figurine> result =
        executeAndGetContent(
            "%s %s".formatted(sqlBuilder, buildOrderByStatement()), params, pageable);

    long totalFigurines = executeAndGetTotal(buildCountStatement().formatted(sqlBuilder), params);
    long totalCollectableFigurines =
        executeAndGetTotal(
            buildCountStatement()
                .formatted("%s %s".formatted(sqlBuilder, buildCollectablePredicate())),
            params);

    return new CollectablePageImpl<>(result, pageable, totalFigurines, totalCollectableFigurines);
  }

  /**
   * Retrieves all figurines matching the provided filter criteria.
   *
   * <p>This method executes a dynamic native query without pagination and applies the default
   * release status ordering.
   *
   * @param filter filtering criteria used to restrict the figurines returned
   * @return list of figurines matching the filter criteria
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
   * <p>The query filters figurines by the year extracted from their release date and applies the
   * default release ordering.
   *
   * @param year release year used to filter figurines
   * @return list of figurines released during the given year
   */
  @Override
  @SuppressWarnings("unchecked")
  public List<Figurine> findAllByYear(int year) {
    SearchQueryContext queryContext = getSearchQueryContext(null);

    StringBuilder sql = queryContext.sql();
    Map<String, Object> params = queryContext.params();

    sql.append(" ").append("AND EXTRACT(YEAR FROM t.release_date) = :year");
    sql.append(" ").append(buildOrderByStatement());
    params.put("year", year);

    Query query = em.createNativeQuery(sql.toString(), Figurine.class);
    queryContext.params().forEach(query::setParameter);

    return query.getResultList();
  }

  /**
   * Builds the SQL query and parameters based on the provided filter.
   *
   * <p>The generated query starts from the base figurine query and dynamically appends predicates
   * only for filter values that are provided.
   *
   * @param filter filter criteria used to build dynamic query conditions
   * @return query context containing the generated SQL and bound parameters
   */
  private SearchQueryContext getSearchQueryContext(FigurineFilter filter) {
    StringBuilder dynamicSql = new StringBuilder(BASE_SQL);
    Map<String, Object> params = new HashMap<>();

    if (Objects.isNull(filter)) {
      return new SearchQueryContext(dynamicSql, params);
    }

    // Dynamic filters
    if (Objects.nonNull(filter.figurineIds()) && !filter.figurineIds().isEmpty()) {
      dynamicSql.append(" AND id IN (:figurineIds)");
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
    if (Objects.nonNull(filter.groupId())) {
      dynamicSql.append(" AND group_id = :groupId");
      params.put("groupId", filter.groupId());
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
    if (Objects.nonNull(filter.releaseStatus())) {
      dynamicSql.append(" AND release_status = :status");
      params.put("status", filter.releaseStatus());
    }

    return new SearchQueryContext(dynamicSql, params);
  }

  /**
   * Builds the SQL ordering clause used to sort figurines by release priority.
   *
   * <p>The ordering prioritizes announced and released figurines before prototypes and rumored
   * items, while sorting each group by the most relevant date.
   *
   * @return SQL order by statement
   */
  private String buildOrderByStatement() {
    return """
              ORDER BY
                  CASE release_status
                      WHEN 'ANNOUNCED' THEN 1
                      WHEN 'RELEASED'  THEN 2
                      WHEN 'PROTOTYPE' THEN 3
                      WHEN 'RUMORED'   THEN 4
                  END,
                  CASE
                      WHEN release_status IN ('ANNOUNCED', 'RELEASED') THEN release_date
                  END DESC,
                  CASE
                      WHEN release_status IN ('ANNOUNCED', 'RELEASED') THEN id
                  END,
                  CASE
                      WHEN release_status = 'PROTOTYPE' THEN announcement_date
                  END DESC,
                  CASE
                      WHEN release_status = 'PROTOTYPE' THEN id
                  END,
                  CASE
                      WHEN release_status = 'RUMORED' THEN creation_date
                  END,
                  CASE
                      WHEN release_status = 'RUMORED' THEN id
                  END
              """;
  }

  /**
   * Builds the SQL predicate used to identify collectable figurines.
   *
   * <p>A figurine is considered collectable when its release status is either: {@code ANNOUNCED} or
   * {@code RELEASED}.
   *
   * @return SQL predicate filtering collectable figurines
   */
  private String buildCollectablePredicate() {
    return "AND RELEASE_STATUS IN ('ANNOUNCED', 'RELEASED')";
  }

  /**
   * Creates a SQL count wrapper query.
   *
   * @return SQL statement template used to count records from a generated query
   */
  private String buildCountStatement() {
    return "SELECT COUNT(*) FROM (%s) count_q";
  }

  /**
   * Executes a paginated native SQL query and maps the result to {@link Figurine} entities.
   *
   * @param sql SQL query to execute
   * @param params query parameters
   * @param pageable pagination configuration
   * @return list of figurines for the requested page
   */
  @SuppressWarnings("unchecked")
  private List<Figurine> executeAndGetContent(
      String sql, Map<String, Object> params, Pageable pageable) {
    Query query = em.createNativeQuery(sql, Figurine.class);
    params.forEach(query::setParameter);

    query.setFirstResult((int) pageable.getOffset());
    query.setMaxResults(pageable.getPageSize());

    return query.getResultList();
  }

  /**
   * Executes a count query and returns the total number of matching records.
   *
   * @param sql SQL count query to execute
   * @param params query parameters
   * @return total number of matching records
   */
  private long executeAndGetTotal(String sql, Map<String, Object> params) {
    Query query = em.createNativeQuery(sql);
    params.forEach(query::setParameter);

    return ((Number) query.getSingleResult()).longValue();
  }
}
