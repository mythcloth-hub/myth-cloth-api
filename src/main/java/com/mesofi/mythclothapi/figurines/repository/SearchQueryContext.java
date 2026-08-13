package com.mesofi.mythclothapi.figurines.repository;

import java.util.Map;

/**
 * Encapsulates the context required to build a dynamic search query.
 *
 * <p>
 * Contains the SQL statement being constructed and the parameters that will be
 * bound to the query.
 * </p>
 *
 * @param sql
 *            the SQL statement being constructed
 * @param params
 *            the named parameters and their corresponding values
 */
public record SearchQueryContext(StringBuilder sql, Map<String, Object> params) {
}