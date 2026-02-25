package com.mesofi.mythclothapi.figurines.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Positive;

import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;

public record DistributorReq(
    @Positive Long supplierId,
    CurrencyCode currency,
    @Positive Double price,
    LocalDate announcedAt,
    LocalDate preorderOpensAt,
    LocalDate releaseDate,
    boolean releaseDateConfirmed) {}
