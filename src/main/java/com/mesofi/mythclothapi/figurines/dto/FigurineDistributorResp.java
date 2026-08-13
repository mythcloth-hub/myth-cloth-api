package com.mesofi.mythclothapi.figurines.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;

/**
 * Represents distributor-specific information for a figurine in an API
 * response.
 *
 * <p>
 * Includes the distributor, currency, pricing, preorder and release dates, and
 * whether the release date has been confirmed.
 *
 * @param distributor
 *            distributor associated with the figurine
 * @param currency
 *            currency used for the listed price
 * @param price
 *            base price of the figurine
 * @param priceWithTax
 *            figurine price including applicable taxes
 * @param announcedAt
 *            date when the figurine was announced by the distributor
 * @param preorderOpensAt
 *            date when preorders become available
 * @param releaseDate
 *            expected or actual release date
 * @param releaseDateConfirmed
 *            whether the release date has been confirmed
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineDistributorResp(DistributorResp distributor, CurrencyCode currency, Double price,
        Double priceWithTax, LocalDate announcedAt, LocalDate preorderOpensAt, LocalDate releaseDate,
        boolean releaseDateConfirmed) {
}