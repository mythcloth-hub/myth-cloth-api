package com.mesofi.mythclothapi.stats.dto;

/**
 * Lightweight figurine reference used in price-based statistics responses.
 *
 * @param id figurine identifier
 * @param name figurine normalized name
 * @param url first official image URL for the figurine
 */
public record FigurinePriceResp(long id, String name, String url) {}
