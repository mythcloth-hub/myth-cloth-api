package com.mesofi.mythclothapi.figurines.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineDistributorResp(
    DistributorResp distributor,
    CurrencyCode currency,
    Double price,
    Double priceWithTax,
    LocalDate announcedAt,
    LocalDate preorderOpensAt,
    LocalDate releaseDate,
    boolean releaseDateConfirmed) {}
