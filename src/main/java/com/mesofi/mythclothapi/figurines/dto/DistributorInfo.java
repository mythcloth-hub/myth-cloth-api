package com.mesofi.mythclothapi.figurines.dto;

import java.time.LocalDate;

import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;

import jakarta.validation.constraints.Positive;

public record DistributorInfo(
    @Positive Long distributorId,
    CurrencyCode currency,
    Double price,
    LocalDate announcementDate,
    LocalDate preorderDate,
    LocalDate releaseDate,
    boolean releaseDateConfirmed) {}
