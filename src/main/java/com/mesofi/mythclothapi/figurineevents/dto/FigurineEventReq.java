package com.mesofi.mythclothapi.figurineevents.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEventType;

import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for creating or updating a {@link
 * com.mesofi.mythclothapi.figurineevents.model.FigurineEvent}.
 *
 * <p>Encapsulates the data provided by the client when recording a lifecycle event for a specific
 * figurine. Validation constraints are enforced at both the controller and service layers.
 *
 * <p>Each event captures:
 *
 * <ul>
 *   <li>A human-readable description of the event
 *   <li>The date on which the event occurred (must be in the past)
 *   <li>The region (country) where the event is applicable
 *   <li>The type of event (e.g., {@code PREORDER_OPEN}, {@code RELEASE})
 *   <li>The ID of the figurine the event belongs to
 * </ul>
 *
 * @see com.mesofi.mythclothapi.figurineevents.model.FigurineEvent
 * @see com.mesofi.mythclothapi.figurineevents.model.FigurineEventType
 * @see com.mesofi.mythclothapi.distributors.model.CountryCode
 */
@Getter
@Setter
public class FigurineEventReq {

  /**
   * A human-readable description of the event.
   *
   * <p>Must not be {@code null} or blank, and cannot exceed 100 characters.
   */
  @NotNull(message = "description must not be blank")
  @Size(max = 100, message = "description must not exceed 100 characters")
  private String description;

  /**
   * The date on which the event occurred.
   *
   * <p>Must not be {@code null} and must be a date in the past.
   */
  @Past
  @NotNull(message = "event date must be provided")
  private LocalDate date;

  /**
   * Indicates whether the event date is confirmed.
   *
   * <p>When {@code true}, the provided date is considered final. When {@code false}, the date is
   * tentative and may change.
   *
   * <p>Defaults to {@code true}.
   */
  private boolean dateConfirmed = true;

  /**
   * The region (country) where this event is applicable.
   *
   * <p>Represents the market or location associated with the event (e.g., {@code JP}, {@code MX}).
   * Must not be {@code null}.
   */
  @NotNull private CountryCode region;

  /**
   * The type of event being recorded.
   *
   * <p>Defines the nature of the event, such as {@code PREORDER_OPEN}, {@code RELEASE}, or {@code
   * RESTOCK}. Must not be {@code null}.
   */
  @NotNull private FigurineEventType type;

  /**
   * The unique identifier of the figurine this event belongs to.
   *
   * <p>Must not be {@code null} and must be a positive value.
   */
  @Positive @NotNull private Long figurineId;
}
