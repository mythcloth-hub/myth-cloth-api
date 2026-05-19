package com.mesofi.mythclothapi.figurines.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.figurines.model.Figurine;

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

  @Override
  @SuppressWarnings("unchecked")
  public Page<Figurine> search(FigurineFilter filter, Pageable pageable) {
    SearchQueryContext queryContext = getSearchQueryContext(filter);

    StringBuilder sql = queryContext.sql();
    Map<String, Object> params = queryContext.params();

    // Sorting (dynamic from Pageable)
    sql.append(" ").append(buildOrderBy());

    Query query = em.createNativeQuery(sql.toString(), Figurine.class);
    params.forEach(query::setParameter);

    // Pagination
    query.setFirstResult((int) pageable.getOffset());
    query.setMaxResults(pageable.getPageSize());

    List<Figurine> result = query.getResultList();

    // Count query (simplified version)
    String countSql = "SELECT COUNT(*) FROM (" + sql + ") count_q";

    Query countQuery = em.createNativeQuery(countSql);
    params.forEach(countQuery::setParameter);

    long total = ((Number) countQuery.getSingleResult()).longValue();

    return new PageImpl<>(result, pageable, total);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Figurine> findAllFigurines(FigurineFilter filter) {
    SearchQueryContext queryContext = getSearchQueryContext(filter);

    Query query = em.createNativeQuery(queryContext.sql().toString(), Figurine.class);
    queryContext.params().forEach(query::setParameter);

    return query.getResultList();
  }

  private SearchQueryContext getSearchQueryContext(FigurineFilter filter) {
    StringBuilder dynamicSql = new StringBuilder(BASE_SQL);
    Map<String, Object> params = new HashMap<>();

    // Dynamic filters
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

  private String buildOrderBy() {
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
                      WHEN release_status = 'RUMORED' THEN creation_date
                  END
              """;
  }
}
