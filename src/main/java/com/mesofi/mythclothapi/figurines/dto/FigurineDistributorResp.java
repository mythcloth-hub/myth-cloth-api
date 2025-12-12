package com.mesofi.mythclothapi.figurines.dto;

import java.time.LocalDate;

import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;

public record FigurineDistributorResp(
    DistributorResp distributor,
    CurrencyCode currency,
    Double price,
    Double priceWithTax,
    LocalDate announcedAt,
    LocalDate preorderOpensAt,
    LocalDate releaseDate,
    boolean releaseDateConfirmed) {}
