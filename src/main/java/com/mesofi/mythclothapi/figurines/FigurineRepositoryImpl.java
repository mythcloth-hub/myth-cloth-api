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
    if (StringUtils.hasLength(filter.name())) {
      sql.append(" AND LOWER(normalized_name) LIKE LOWER(:name)");
      params.put("name", "%" + filter.name() + "%");
    }
    if (Objects.nonNull(filter.lineUpId())) {
      sql.append(" AND lineup_id = :lineUpId");
      params.put("lineUpId", filter.lineUpId());
    }
    if (Objects.nonNull(filter.seriesId())) {
      sql.append(" AND series_id = :seriesId");
      params.put("seriesId", filter.seriesId());
    }
    if (Objects.nonNull(filter.groupId())) {
      sql.append(" AND group_id = :groupId");
      params.put("groupId", filter.groupId());
    }
    if (Objects.nonNull(filter.metalBody())) {
      sql.append(" AND is_metal_body = :metalBody");
      params.put("metalBody", filter.metalBody());
    }
    if (Objects.nonNull(filter.oce())) {
      sql.append(" AND is_oce = :oce");
      params.put("oce", filter.oce());
    }
    if (Objects.nonNull(filter.revival())) {
      sql.append(" AND is_revival = :revival");
      params.put("revival", filter.revival());
    }
    if (Objects.nonNull(filter.plainCloth())) {
      sql.append(" AND is_plain_cloth = :plainCloth");
      params.put("plainCloth", filter.plainCloth());
    }
    if (Objects.nonNull(filter.broken())) {
      sql.append(" AND is_broken = :broken");
      params.put("broken", filter.broken());
    }
    if (Objects.nonNull(filter.golden())) {
      sql.append(" AND is_golden = :golden");
      params.put("golden", filter.golden());
    }
    if (Objects.nonNull(filter.gold())) {
      sql.append(" AND is_gold = :gold");
      params.put("gold", filter.gold());
    }
    if (Objects.nonNull(filter.manga())) {
      sql.append(" AND is_manga = :manga");
      params.put("manga", filter.manga());
    }
    if (Objects.nonNull(filter.set())) {
      sql.append(" AND is_set = :set");
      params.put("set", filter.set());
    }
    if (Objects.nonNull(filter.articulable())) {
      sql.append(" AND is_articulable = :articulable");
      params.put("articulable", filter.articulable());
    }
    if (Objects.nonNull(filter.releaseStatus())) {
      sql.append(" AND release_status = :status");
      params.put("status", filter.releaseStatus());
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
