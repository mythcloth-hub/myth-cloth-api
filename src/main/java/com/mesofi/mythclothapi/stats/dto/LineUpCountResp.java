package com.mesofi.mythclothapi.stats.dto;

/**
 * Count of figurines grouped by line-up description.
 *
 * @param line line-up description
 * @param count total figurines in the line-up
 */
public record LineUpCountResp(String line, int count) {}
