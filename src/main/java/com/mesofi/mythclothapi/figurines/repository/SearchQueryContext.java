package com.mesofi.mythclothapi.figurines.repository;

import java.util.Map;

public record SearchQueryContext(StringBuilder sql, Map<String, Object> params) {}
