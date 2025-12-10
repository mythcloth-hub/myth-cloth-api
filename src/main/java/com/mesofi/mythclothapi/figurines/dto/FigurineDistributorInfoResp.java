package com.mesofi.mythclothapi.figurines.dto;

import java.time.LocalDate;

import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;

public record FigurineDistributorInfoResp(
    Distributor distributor,
    CurrencyCode currency,
    Double price,
    LocalDate announcementDate,
    LocalDate preorderDate,
    LocalDate releaseDate,
    boolean releaseDateConfirmed) {}
