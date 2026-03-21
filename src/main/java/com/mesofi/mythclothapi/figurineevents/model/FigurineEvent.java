package com.mesofi.mythclothapi.figurineevents.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.mesofi.mythclothapi.common.Descriptive;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.figurines.model.Figurine;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an event associated with a {@link Figurine}.
 *
 * <p>A {@code FigurineEvent} captures a dated action or milestone in the lifecycle of a figurine,
 * such as a pre-order, release, arrival, purchase, or any other relevant event tracked by the
 * system.
 *
 * <p>Each event is characterized by:
 *
 * <ul>
 *   <li>The date when the event occurred
 *   <li>The type of event
 *   <li>The region (country) where the event is relevant
 *   <li>The figurine to which the event belongs
 * </ul>
 *
 * <p>This entity extends {@link Descriptive}, inheriting a unique identifier, a human-readable
 * description, and other shared descriptive fields across domain entities.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "figurine_events")
public class FigurineEvent extends Descriptive {

  /**
   * The date on which the event occurred.
   *
   * <p>This field is mandatory and cannot be {@code null}. It represents the effective date of the
   * event (e.g., release date, purchase date, arrival date).
   */
  @Column(nullable = false)
  private LocalDate eventDate;

  /**
   * The region (country) associated with this event.
   *
   * <p>This typically represents the market or location where the event is applicable, such as the
   * country of release or purchase.
   *
   * <p>This field is mandatory and cannot be {@code null}.
   */
  @Column(nullable = false)
  private CountryCode region;

  /**
   * The type of event being recorded.
   *
   * <p>Defines the nature of the event (e.g., PRE_ORDER, RELEASE, ARRIVAL, PURCHASE).
   *
   * <p>This field is mandatory and cannot be {@code null}.
   */
  @Column(nullable = false)
  private FigurineEventType type;

  /**
   * The figurine to which this event belongs.
   *
   * <p>Defined as a {@code ManyToOne} relationship since a single figurine can have multiple
   * associated events throughout its lifecycle.
   *
   * <p>Uses lazy loading to avoid unnecessary data retrieval unless the figurine reference is
   * explicitly accessed.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "figurine_id", nullable = false)
  private Figurine figurine;
}
