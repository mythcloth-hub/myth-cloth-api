package com.mesofi.mythclothapi.collectorspurchases.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import com.mesofi.mythclothapi.common.Auditable;
import com.mesofi.mythclothapi.common.CurrencyCode;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "collector_purchases")
public class CollectorPurchase extends Auditable {

    /**
     * The date of the order. Cannot be a future date.
     */
    @Column(nullable = false, comment = "The date of the order. Cannot be a future date.")
    private LocalDate orderDate;

    /**
     * The seller from whom the purchase was made.
     */
    @Column(length = 150, comment = "The seller from whom the purchase was made.")
    private String seller;

    /**
     * The order number of the purchase if provided by the seller.
     */
    @Column(length = 50, comment = "The order number of the purchase if provided by the seller.")
    private String orderNumber;

    /**
     * The currency of the purchase.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 3, comment = "The ISO 4217 currency code of the purchase.")
    private CurrencyCode currency;

    /**
     * The total number of figurines in the purchase. Defaults to 1.
     */
    private Integer totalFigurines = 1;

    /**
     * The total amount of the purchase.
     */
    private BigDecimal totalAmount;

    /**
     * The shipping status of the purchase.
     */
    @Enumerated(EnumType.STRING)
    private ShippingStatus shippingStatus;

    /**
     * The tracking number of the shipment if provided by the seller.
     */
    @Column(length = 100, comment = "The tracking number of the shipment if provided by the seller.")
    private String trackingNumber;

    /**
     * The carrier of the shipment if provided by the seller.
     */
    @Column(length = 100, comment = "The carrier of the shipment if provided by the seller.")
    private String carrier;

    /**
     * The date when the shipment was sent.
     */
    private LocalDate shippedDate;

    /**
     * The date when the shipment was delivered.
     */
    private LocalDate deliveredDate;
}
