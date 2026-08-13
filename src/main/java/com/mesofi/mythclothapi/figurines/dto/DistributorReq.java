package com.mesofi.mythclothapi.figurines.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;

/**
 * Represents distributor-specific information supplied when creating or
 * updating a figurine.
 *
 * <p>
 * The request identifies the supplier and currency and optionally provides
 * pricing, announcement, preorder, and release information.
 *
 * @param supplierId
 *            identifier of the supplier; must be positive
 * @param currency
 *            currency in which the price is specified
 * @param price
 *            distributor price; must be positive when provided
 * @param announcedAt
 *            date when the figurine was announced
 * @param preorderOpensAt
 *            date when preorders become available
 * @param releaseDate
 *            expected or actual release date
 * @param releaseDateConfirmed
 *            whether the release date has been confirmed; defaults to
 *            {@code false} when not provided
 */
public record DistributorReq(@NotNull @Positive Long supplierId, @NotNull CurrencyCode currency, @Positive Double price,
        LocalDate announcedAt, LocalDate preorderOpensAt, LocalDate releaseDate, Boolean releaseDateConfirmed) {

    /**
     * Creates a distributor request and defaults an unspecified release-date
     * confirmation flag to {@code false}.
     */
    public DistributorReq {
        if (releaseDateConfirmed == null) {
            releaseDateConfirmed = false;
        }
    }
}