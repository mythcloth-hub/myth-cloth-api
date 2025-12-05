package com.mesofi.mythclothapi.figurines.dto;

import java.time.LocalDate;

import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;

public record DistributorInfo(
    Long distributorId,
    CurrencyCode currency,
    Double price,
    LocalDate announcementDate,
    LocalDate preorderDate,
    LocalDate releaseDate,
    boolean releaseDateConfirmed) {}
