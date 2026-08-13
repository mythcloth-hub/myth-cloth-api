package com.mesofi.mythclothapi.figurines.mapper;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a date together with an indication of whether the date was
 * explicitly confirmed in the source data.
 *
 * <p>
 * This class is used during figurine data mapping to distinguish between dates
 * that are fully specified and confirmed and dates that are inferred from
 * partial source information.
 * </p>
 */
@Getter
@AllArgsConstructor
public class LocalDateConfirmed {

    /**
     * The mapped date.
     */
    private LocalDate date;

    /**
     * Indicates whether the date was explicitly confirmed in the source data.
     */
    private boolean confirmed;
}