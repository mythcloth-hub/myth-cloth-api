package com.mesofi.mythclothapi.figurines.mapper;

import com.mesofi.mythclothapi.anniversaries.model.AnniversaryType;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents an anniversary number together with its optional anniversary type
 * during figurine CSV import processing.
 *
 * <p>
 * The anniversary number identifies the anniversary year, while the
 * {@link AnniversaryType} provides additional classification when available.
 * The type may be {@code null} when the source data specifies only the
 * anniversary number.
 */
@Getter
@AllArgsConstructor
public class AnniversaryNumberType {

    /** The type of anniversary, or {@code null} when no type was specified. */
    private AnniversaryType anniversaryType;

    /** The anniversary number or year associated with the figurine. */
    private int anniversaryNumber;
}