package com.mesofi.mythclothapi.figurines.dto;

import java.time.LocalDate;

import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;

import jakarta.validation.constraints.Positive;

public record DistributorReq(
    @Positive Long supplierId,
    CurrencyCode currency,
    @Positive Double price,
    LocalDate announcedAt,
    LocalDate preorderOpensAt,
    LocalDate releaseDate,
    boolean releaseDateConfirmed) {}
