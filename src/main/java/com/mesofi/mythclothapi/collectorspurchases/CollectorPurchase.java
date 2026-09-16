package com.mesofi.mythclothapi.collectorspurchases;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchaseFigurine;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseChannel;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;
import com.mesofi.mythclothapi.common.Auditable;
import com.mesofi.mythclothapi.common.CurrencyCode;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a purchase made by a collector, including details such as the
 * order date, seller information, currency, total amount, purchase channel,
 * shipping status, and associated figurines.
 */
@Entity
@Getter
@Setter
@Table(name = "collector_purchases")
public class CollectorPurchase extends Auditable {

    /**
     * The collector who made the purchase. This is a mandatory relationship, and
     * the collector is fetched lazily to optimize performance.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collector_id", nullable = false)
    private Collector collector;

    /**
     * The date when the order was placed. This field is mandatory and cannot be
     * null.
     */
    @Column(nullable = false, comment = "The date when the order was placed, cannot be a future date")
    private LocalDate orderDate;

    /**
     * The name of the seller from whom the purchase was made. This field has a
     * maximum length of 150 characters.
     */
    @Column(nullable = false, length = 150, comment = "The name of the seller from whom the purchase was made, maximum length of 150 characters")
    private String seller;

    /**
     * The order number of the purchase if provided by the seller. This field has a
     * maximum length of 50 characters.
     */
    @Column(length = 50, comment = "The order number of the purchase if provided by the seller.")
    private String orderNumber;

    /**
     * The currency in which the purchase was made. This field is mandatory and has
     * a maximum length of 3 characters.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3, comment = "The currency in which the purchase was made, maximum length of 3 characters")
    private CurrencyCode currency;

    /**
     * The channel through which the purchase was made. This field is mandatory and
     * has a maximum length of 20 characters.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, comment = "The channel through which the purchase was made, maximum length of 20 characters")
    private PurchaseChannel purchaseChannel;

    /**
     * The shipping status of the purchase. This field has a maximum length of 20
     * characters.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, comment = "The shipping status of the purchase, maximum length of 20 characters")
    private ShippingStatus shippingStatus;

    /**
     * The tracking number for the shipment. This field has a maximum length of 100
     * characters.
     */
    @Column(length = 100)
    private String trackingNumber;

    /**
     * The carrier responsible for the shipment. This field has a maximum length of
     * 100 characters.
     */
    @Column(length = 100)
    private String carrier;

    /**
     * The date when the purchase was shipped.
     */
    private LocalDate shippedDate;

    /**
     * The date when the purchase was delivered.
     */
    private LocalDate deliveredDate;

    /**
     * The list of figurines associated with this purchase. This is a one-to-many
     * relationship, and the figurines are managed with cascade operations and
     * orphan removal.
     */
    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectorPurchaseFigurine> figurines = new ArrayList<>();
}
