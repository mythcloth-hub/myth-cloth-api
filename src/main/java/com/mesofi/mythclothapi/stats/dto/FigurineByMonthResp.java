package com.mesofi.mythclothapi.stats.dto;

/**
 * Lightweight figurine reference used in month-based statistics responses.
 *
 * @param id figurine identifier
 * @param name figurine normalized name
 * @param url first official image URL for the figurine
 */
public record FigurineByMonthResp(Long id, String name, String url) {}
