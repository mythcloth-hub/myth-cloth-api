package com.mesofi.mythclothapi.figurineevents.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEventType;

/**
 * Response DTO representing a {@link com.mesofi.mythclothapi.figurineevents.model.FigurineEvent}.
 *
 * <p>Returned by the API after creating, retrieving, or updating a figurine event. Null or empty
 * fields are excluded from the serialized JSON output.
 *
 * @param id the unique identifier of the event
 * @param date the date on which the event occurred
 * @param type the nature of the event (e.g., {@code PREORDER_OPEN}, {@code RELEASE})
 * @param region the country or market where the event is applicable
 * @param description a human-readable description of the event
 * @see com.mesofi.mythclothapi.figurineevents.model.FigurineEvent
 * @see com.mesofi.mythclothapi.figurineevents.model.FigurineEventType
 * @see com.mesofi.mythclothapi.distributors.model.CountryCode
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineEventResp(
    long id, LocalDate date, FigurineEventType type, CountryCode region, String description) {}
