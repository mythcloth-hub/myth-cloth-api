package com.mesofi.mythclothapi.figurineevents.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.mesofi.mythclothapi.common.Auditable;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.figurines.model.Figurine;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an event associated with a {@link Figurine}.
 *
 * <p>
 * A {@code FigurineEvent} captures a dated event or milestone in the lifecycle
 * of a figurine, such as an announcement, prototype presentation, pre-order,
 * release, or other relevant event tracked by the system.
 * </p>
 *
 * <p>
 * Each event is characterized by:
 * </p>
 * <ul>
 * <li>the {@link #eventDate event date},</li>
 * <li>whether the event date has been officially confirmed,</li>
 * <li>the {@link #region region} where the event applies,</li>
 * <li>the {@link #type type} of event,</li>
 * <li>additional {@link #details details} describing the event, and</li>
 * <li>the {@link #figurine figurine} associated with the event.</li>
 * </ul>
 *
 * <p>
 * This entity extends {@link Auditable}, inheriting common auditing fields used
 * to track the creation and modification of domain entities.
 * </p>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "figurine_events")
public class FigurineEvent extends Auditable {

    /**
     * Additional information describing the event.
     *
     * <p>
     * This field provides contextual information about the event that cannot be
     * fully represented by the event type, date, or region.
     * </p>
     *
     * <p>
     * The field is mandatory, must be unique, and is limited to 200 characters.
     * </p>
     */
    @Column(nullable = false, length = 200)
    private String details;

    /**
     * The date on which the event occurred.
     *
     * <p>
     * This field is mandatory and cannot be {@code null}. It represents the
     * effective date of the event (e.g., release date, purchase date, arrival
     * date).
     */
    @Column(nullable = false)
    private LocalDate eventDate;

    /**
     * Indicates whether the {@link #eventDate} has been officially confirmed.
     *
     * <p>
     * This flag distinguishes tentative or estimated dates from dates that have
     * been formally announced by the manufacturer, distributor, or other
     * authoritative source.
     *
     * <p>
     * A value of {@code true} means the event date is confirmed, while
     * {@code false} means it is still provisional.
     */
    @Column(nullable = false)
    private boolean eventDateConfirmed;

    /**
     * The region (country) associated with this event.
     *
     * <p>
     * This typically represents the market or location where the event is
     * applicable, such as the country of release or purchase.
     *
     * <p>
     * This field is mandatory and cannot be {@code null}.
     */
    @Column(nullable = false)
    private CountryCode region;

    /**
     * The type of event being recorded.
     *
     * <p>
     * Defines the nature of the event (e.g., PRE_ORDER, RELEASE, ARRIVAL,
     * PURCHASE).
     *
     * <p>
     * This field is mandatory and cannot be {@code null}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FigurineEventType type;

    /**
     * The figurine to which this event belongs.
     *
     * <p>
     * Defined as a {@code ManyToOne} relationship since a single figurine can have
     * multiple associated events throughout its lifecycle.
     *
     * <p>
     * Uses lazy loading to avoid unnecessary data retrieval unless the figurine
     * reference is explicitly accessed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "figurine_id", nullable = false)
    private Figurine figurine;
}
