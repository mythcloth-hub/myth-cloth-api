package com.mesofi.mythclothapi.stats.dto;

import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;

/**
 * Lightweight figurine reference used in month-based statistics responses.
 *
 * @param id
 *            figurine identifier
 * @param name
 *            figurine normalized name
 * @param url
 *            first official image URL for the figurine
 * @param releaseStatus
 *            figurine release status
 */
public record FigurineByMonthResp(Long id, String name, String url, ReleaseStatus releaseStatus) {
}
