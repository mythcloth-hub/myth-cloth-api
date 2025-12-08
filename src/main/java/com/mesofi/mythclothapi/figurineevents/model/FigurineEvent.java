package com.mesofi.mythclothapi.figurineevents.model;

import java.time.LocalDate;

import com.mesofi.mythclothapi.common.Descriptive;
import com.mesofi.mythclothapi.figurines.model.Figurine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an event associated with a {@link Figurine}.
 *
 * <p>A {@code FigurineEvent} captures a dated action or milestone in the lifecycle of a figurine
 * (e.g., purchase date, arrival date, pre-order, etc.).
 *
 * <p>Extends {@link Descriptive}, inheriting an ID, a description, and other shared descriptive
 * fields across domain entities.
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
   * <p>This field is mandatory and cannot be {@code null}.
   */
  @Column(nullable = false)
  private LocalDate eventDate;

  /**
   * The figurine to which this event belongs.
   *
   * <p>Defined as a {@code ManyToOne} relationship since a figurine can have multiple events
   * associated with it.
   *
   * <p>Uses lazy loading to avoid unnecessary data retrieval unless the figurine reference is
   * explicitly accessed.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "figurine_id", nullable = false)
  private Figurine figurine;
}
