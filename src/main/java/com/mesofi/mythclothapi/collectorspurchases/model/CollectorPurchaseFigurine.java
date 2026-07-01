package com.mesofi.mythclothapi.collectorspurchases.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.mesofi.mythclothapi.common.BaseId;
import com.mesofi.mythclothapi.figurines.model.Figurine;

import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing a figurine included in a collector purchase.
 *
 * <p>This entity acts as a purchase line item, storing the figurine acquired, quantity purchased,
 * price paid, and purchase type.
 *
 * <p>Purchase line items preserve historical transaction information and are independent from the
 * collector's current collection quantities.
 *
 * <p>A figurine can only appear once within the same purchase. If multiple units are purchased, the
 * quantity field represents the amount acquired.
 */
@Entity
@Getter
@Setter
@Table(
    name = "collector_purchase_figurines",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_purchase_figurine_pair",
            columnNames = {"purchase_id", "figurine_id"}))
public class CollectorPurchaseFigurine extends BaseId {

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private CollectorPurchase purchase;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Figurine figurine;

  private Integer quantity;

  private BigDecimal pricePaid;

  @Enumerated(EnumType.STRING)
  private PurchaseType purchaseType;
}
