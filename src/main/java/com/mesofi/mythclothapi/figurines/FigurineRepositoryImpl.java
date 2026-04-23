package com.mesofi.mythclothapi.figurines;

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

import com.mesofi.mythclothapi.figurines.model.Figurine;

@Repository
public class FigurineRepositoryImpl implements FigurineRepositoryCustom {

  @PersistenceContext private EntityManager em;

  @Override
  public Page<Figurine> search(FigurineFilter filter, Pageable pageable) {

    StringBuilder sql =
        new StringBuilder(
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
	                """);

    Map<String, Object> params = new HashMap<>();

    // Dynamic filters
    if (StringUtils.hasLength(filter.getName())) {
      sql.append(" AND LOWER(normalized_name) LIKE LOWER(:name)");
      params.put("name", "%" + filter.getName() + "%");
    }
    if (Objects.nonNull(filter.getLineUpId())) {
      sql.append(" AND lineup_id = :lineUpId");
      params.put("lineUpId", filter.getLineUpId());
    }
    if (Objects.nonNull(filter.getSeriesId())) {
      sql.append(" AND series_id = :seriesId");
      params.put("seriesId", filter.getSeriesId());
    }

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

  private String buildOrderBy() {
    StringBuilder sql =
        new StringBuilder(
            """
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
	                        WHEN release_status = 'PROTOTYPE' THEN announcement_date
	                    END DESC,
	                    CASE
	                        WHEN release_status = 'RUMORED' THEN creation_date
	                    END
	                """);
    return sql.toString();
  }
}
