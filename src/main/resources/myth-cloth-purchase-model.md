# Myth Cloth Collector Purchase Model

## Overview

This document describes the final purchase-tracking model for the Myth Cloth collection application.

The model separates:

1. **Purchase/order** — the transaction with a seller.
2. **Purchase figurine line** — a figurine and quantity included in the purchase.
3. **Shipment** — a physical shipment associated with an order.
4. **Shipment figurine allocation** — how many units of each purchase line are included in a shipment.

This supports physical-store purchases, normal online orders, multiple shipments, partial shipments, preorders, and quantity splits.

---

## Domain Model

```text
Collector
   |
   | 1
   | 
   | *
CollectorPurchase
   |
   +-----------------------------+
   |                             |
   | 1                           | 1
   | *                           | *
CollectorPurchaseFigurine     CollectorPurchaseShipment
   |                             |
   | 1                           | 1
   | *                           | *
   +------ CollectorPurchaseShipmentFigurine ------+
```

The shipment allocation entity connects a purchase line to a shipment.

A purchase line can be split across multiple shipments:

```text
Purchase #1001
├── Pegasus Seiya × 3
│   ├── Shipment #1 → 1
│   └── Shipment #2 → 2
│
└── Dragon Shiryu × 1
    └── Shipment #1 → 1
```

---

# 1. CollectorPurchase

Represents the overall transaction/order.

```java
@Entity
@Getter
@Setter
@Table(name = "collector_purchases")
public class CollectorPurchase extends Auditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collector_id", nullable = false)
    private Collector collector;

    @Column(nullable = false, comment = "The date of the order. Cannot be a future date.")
    private LocalDate orderDate;

    @Column(length = 150, comment = "The seller from whom the purchase was made.")
    private String seller;

    @Column(length = 50, comment = "The order number of the purchase if provided by the seller.")
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3,
            comment = "The ISO 4217 currency code of the purchase.")
    private CurrencyCode currency;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseChannel purchaseChannel;

    @OneToMany(
        mappedBy = "purchase",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<CollectorPurchaseFigurine> purchaseFigurines = new ArrayList<>();

    @OneToMany(
        mappedBy = "purchase",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<CollectorPurchaseShipment> shipments = new ArrayList<>();
}
```

### Important design decisions

### `seller`

Use `seller` rather than `store`.

A purchase could come from:

- an official store
- a marketplace seller
- a distributor
- a convention vendor
- an individual collector
- a second-hand seller

### `orderNumber`

The order number is optional because not every seller provides one.

### `currency`

Currency is represented by the shared application-level `CurrencyCode` enum.

```java
public enum CurrencyCode {
    JPY,
    MXN,
    EUR,
    USD,
    CNY,
    CAD
}
```

It belongs in a common package because currency is not specific to purchases.

### `totalAmount`

`totalAmount` represents the total monetary amount recorded for the purchase.

Use:

```java
@Column(precision = 12, scale = 2)
private BigDecimal totalAmount;
```

Do not use floating-point types for money.

### `purchaseChannel`

Recommended enum:

```java
public enum PurchaseChannel {
    ONLINE,
    PHYSICAL_STORE
}
```

This explicitly distinguishes an online purchase from a physical-store purchase.

---

# 2. CollectorPurchaseFigurine

Represents one figurine line within a purchase.

It intentionally references `CollectorCollectionFigurine`, rather than the catalog `Figurine`.

```java
@Entity
@Getter
@Setter
@Table(
    name = "collector_purchase_figurines",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_purchase_collection_figurine",
        columnNames = {"purchase_id", "collection_figurine_id"}
    )
)
public class CollectorPurchaseFigurine extends Auditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_id", nullable = false)
    private CollectorPurchase purchase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_figurine_id", nullable = false)
    private CollectorCollectionFigurine collectionFigurine;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 12, scale = 2)
    private BigDecimal pricePaid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseType purchaseType;

    @OneToMany(
        mappedBy = "purchaseFigurine",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<CollectorPurchaseShipmentFigurine> shipmentFigurines =
        new ArrayList<>();
}
```

## Why reference `CollectorCollectionFigurine`?

Suppose the collection contains:

```text
Saint Seiya collection
└── Pegasus Seiya
    quantity = 3
```

The collector might acquire those three units through two purchases:

```text
Purchase #1001
└── Pegasus Seiya × 2

Purchase #1002
└── Pegasus Seiya × 1
```

Therefore:

```text
CollectorCollectionFigurine
        1
        |
        | *
        v
CollectorPurchaseFigurine
```

One collection figurine can have many purchase lines.

This also allows purchase lines to follow the lifecycle of the collector's collection figurine.

---

# 3. CollectorPurchaseShipment

Represents an actual shipment associated with an order.

```java
@Entity
@Getter
@Setter
@Table(name = "collector_purchase_shipments")
public class CollectorPurchaseShipment extends Auditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_id", nullable = false)
    private CollectorPurchase purchase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShippingStatus shippingStatus;

    @Column(length = 100,
            comment = "The tracking number of the shipment if available.")
    private String trackingNumber;

    @Column(length = 100,
            comment = "The carrier used to ship the purchase, if available.")
    private String carrier;

    private LocalDate shippedDate;

    private LocalDate deliveredDate;

    @OneToMany(
        mappedBy = "shipment",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<CollectorPurchaseShipmentFigurine> shipmentFigurines =
        new ArrayList<>();
}
```

Recommended shipping status:

```java
public enum ShippingStatus {
    PENDING,
    SHIPPED,
    READY_TO_PICKUP,
    DELIVERED
}
```

`PREORDER` should **not** be a shipping status.

A preorder describes how/when the item was purchased, while shipping status describes the physical shipment lifecycle.

---

# 4. CollectorPurchaseShipmentFigurine

This is the allocation entity between a purchase line and a shipment.

```java
@Entity
@Getter
@Setter
@Table(
    name = "collector_purchase_shipment_figurines",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_shipment_purchase_figurine",
        columnNames = {"shipment_id", "purchase_figurine_id"}
    )
)
public class CollectorPurchaseShipmentFigurine extends Auditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private CollectorPurchaseShipment shipment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_figurine_id", nullable = false)
    private CollectorPurchaseFigurine purchaseFigurine;

    @Column(nullable = false)
    private Integer quantity;
}
```

The important field is:

```java
private Integer quantity;
```

It answers:

> How many units from this purchase line are included in this shipment?

---

# PurchaseType

```java
public enum PurchaseType {
    RETAIL,
    PREORDER,
    SECOND_HAND,
}
```

Suggested meanings:

- `RETAIL` — purchased as a new item directly from a retail seller.
- `PREORDER` — purchased before official release or availability.
- `SECOND_HAND` — purchased from a previous owner or the secondary market.

### Important semantic note

`RETAIL` and `PREORDER` can overlap conceptually.

For example:

```text
Preordered from a retail store
```

is both a retail purchase and a preorder.

If the application eventually needs to distinguish those dimensions independently, consider separating:

```text
Acquisition source:
    RETAIL
    SECOND_HAND

Purchase timing:
    STANDARD
    PREORDER
```

For the current application, the simpler enum is acceptable if those distinctions are not needed elsewhere.

---

# CurrencyCode

Place this in the common domain/package because it is shared across domains.

```java
public enum CurrencyCode {
    JPY,
    MXN,
    EUR,
    USD,
    CNY,
    CAD
}
```

Example:

```java
purchase.setCurrency(CurrencyCode.JPY);
```

---

# PurchaseChannel

```java
public enum PurchaseChannel {
    ONLINE,
    PHYSICAL_STORE
}
```

This should be stored on `CollectorPurchase`.

Do not infer the channel from whether shipments exist.

---

# Physical Store Purchase

A physical purchase can have no shipments.

Example:

```text
Purchase #P-2026-001

Channel:
    PHYSICAL_STORE

Seller:
    Local collectible shop

Order date:
    2026-09-15

Purchase figurines:
    Pegasus Seiya × 1
    Dragon Shiryu × 1

Shipments:
    none
```

Database relationship:

```text
CollectorPurchase
├── purchaseChannel = PHYSICAL_STORE
├── purchaseFigurines = 2 lines
└── shipments = 0
```

This is intentional.

The purchase is complete without creating an artificial shipment.

---

# Normal Online Order

Example:

```text
Order #AMZ-12345

Channel:
    ONLINE

Seller:
    Marketplace Seller

Purchase lines:
    Pegasus Seiya × 1
    Dragon Shiryu × 1

Shipment #1:
    Pegasus Seiya × 1
    Dragon Shiryu × 1
    status = DELIVERED
```

Relationships:

```text
Purchase
├── Pegasus × 1
├── Dragon × 1
│
└── Shipment #1
    ├── Pegasus × 1
    └── Dragon × 1
```

---

# Multiple Shipments

An online order can be split into multiple shipments.

Example:

```text
Order #1001

Purchase lines:
    Pegasus Seiya × 1
    Dragon Shiryu × 1
    Andromeda Shun × 1

Shipment #1
    Pegasus Seiya × 1
    Dragon Shiryu × 1
    status = DELIVERED

Shipment #2
    Andromeda Shun × 1
    status = SHIPPED
```

The purchase itself remains one order.

```text
CollectorPurchase #1001
├── PurchaseFigurine: Pegasus × 1
├── PurchaseFigurine: Dragon × 1
├── PurchaseFigurine: Shun × 1
│
├── Shipment #1
│   ├── Pegasus × 1
│   └── Dragon × 1
│
└── Shipment #2
    └── Shun × 1
```

---

# Quantity Split Across Shipments

This is one of the main reasons for the fourth entity.

Suppose:

```text
Purchase:
    Pegasus Seiya × 3
```

The seller sends them in two shipments:

```text
Shipment #1:
    Pegasus × 1

Shipment #2:
    Pegasus × 2
```

The data becomes:

```text
CollectorPurchaseFigurine
    quantity = 3

CollectorPurchaseShipmentFigurine
    shipment = #1
    quantity = 1

CollectorPurchaseShipmentFigurine
    shipment = #2
    quantity = 2
```

This would not be modeled correctly by simply putting `shipment_id` on `CollectorPurchaseFigurine`.

---

# Partial Shipment

A purchase line does not need to be completely shipped.

Example:

```text
Purchase:
    Pegasus × 5

Shipment #1:
    Pegasus × 2
```

Current allocation:

```text
purchased quantity = 5
shipped quantity = 2
remaining quantity = 3
```

The invariant should be:

```text
SUM(shipment allocations)
    <=
purchase line quantity
```

For a fully shipped line:

```text
SUM(shipment allocations)
    =
purchase line quantity
```

For a partially shipped line:

```text
SUM(shipment allocations)
    <
purchase line quantity
```

This validation should primarily be enforced in the service layer.

---

# Preorder Example

```text
Purchase #PRE-500

Purchase line:
    New Myth Cloth × 1
    purchaseType = PREORDER

Shipment:
    none yet
```

Later:

```text
Shipment #900

status = SHIPPED

Shipment allocation:
    New Myth Cloth × 1
```

The preorder remains a property of the purchase line.

The shipment status is independent.

---

# Mixed Release Dates

One online order can contain products released at different times.

Example:

```text
Order #2001

Purchase lines:
    Pegasus × 1
    Future Myth Cloth × 1
```

The seller might send:

```text
Shipment #1
    Pegasus × 1
    status = DELIVERED

Shipment #2
    Future Myth Cloth × 1
    status = PENDING
```

No special order-level logic is required.

The shipment model naturally supports this.

---

# Collection Relationship

The intended relationship is:

```text
CollectorCollection
    |
    | 1
    | *
CollectorCollectionFigurine
    |
    | 1
    | *
CollectorPurchaseFigurine
```

The collection figurine can optionally expose its purchase lines:

```java
@OneToMany(
    mappedBy = "collectionFigurine",
    cascade = CascadeType.ALL,
    orphanRemoval = true
)
private List<CollectorPurchaseFigurine> purchaseFigurines =
    new ArrayList<>();
```

The precise name `purchaseFigurines` is preferable to `purchases` because these are purchase **line items**, not purchases.

---

# Lifecycle and Cascade Rules

## Purchase → Purchase Lines

Use:

```java
@OneToMany(
    mappedBy = "purchase",
    cascade = CascadeType.ALL,
    orphanRemoval = true
)
```

Deleting a purchase deletes its purchase lines.

Removing a purchase line from the purchase deletes that line.

## Purchase → Shipments

Use:

```java
@OneToMany(
    mappedBy = "purchase",
    cascade = CascadeType.ALL,
    orphanRemoval = true
)
```

Deleting a purchase deletes its shipments and shipment allocations.

## Shipment → Shipment Lines

Use:

```java
@OneToMany(
    mappedBy = "shipment",
    cascade = CascadeType.ALL,
    orphanRemoval = true
)
```

Deleting a shipment deletes its shipment allocation records.

## Purchase Line → Shipment Lines

Use:

```java
@OneToMany(
    mappedBy = "purchaseFigurine",
    cascade = CascadeType.ALL,
    orphanRemoval = true
)
```

Deleting a purchase line removes its shipment allocations.

---

# Empty Purchase Rule

`orphanRemoval = true` does **not** mean:

> If the purchase has zero lines, delete the purchase automatically.

It only removes child entities that are orphaned.

Therefore, if the application requires:

```text
Every purchase must contain at least one purchase line
```

the service layer should explicitly enforce it.

Example:

```java
if (purchase.getPurchaseFigurines().isEmpty()) {
    purchaseRepository.delete(purchase);
}
```

This is preferable to relying on accidental ORM behavior.

---

# Empty Shipment Rule

The same principle applies to shipments.

If the domain requires:

```text
Every shipment must contain at least one figurine allocation
```

enforce this in the service layer.

Do not assume JPA will automatically delete an empty shipment.

---

# Collector Relationship

The purchase has a mandatory collector:

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "collector_id", nullable = false)
private Collector collector;
```

The relationship does not need to be bidirectional.

A unidirectional relationship is preferable unless the application actually needs:

```java
collector.getPurchases()
```

If a reverse relationship is later added, avoid:

```java
cascade = CascadeType.ALL
orphanRemoval = true
```

for historical purchases unless deleting a collector is explicitly intended to delete all purchase history.

Purchase history is generally safer when its lifecycle is explicitly controlled.

---

# Important Validation Rules

The service layer should enforce at least these rules.

## 1. Order date cannot be in the future

```text
orderDate <= today
```

## 2. Purchase must contain at least one figurine line

```text
purchaseFigurines.size() > 0
```

## 3. Purchase line quantity must be positive

```text
quantity > 0
```

## 4. Shipment allocation quantity must be positive

```text
quantity > 0
```

## 5. Shipment allocation cannot exceed purchased quantity

```text
SUM(allocations.quantity) <= purchaseFigurine.quantity
```

## 6. Shipment and purchase line must belong to the same purchase

For:

```text
Shipment S
Purchase P
PurchaseLine L
```

validate:

```text
S.purchase == L.purchase
```

This relationship is not automatically enforced by the simple JPA mappings.

## 7. Shipment dates should be consistent

Recommended rules:

```text
shippedDate is required when status = SHIPPED or later
deliveredDate is required when status = DELIVERED
deliveredDate >= shippedDate
```

The exact rules can be relaxed if the application wants to support incomplete historical data.

---

# Why Four Entities?

A simpler three-entity design might be:

```text
Purchase
PurchaseFigurine
Shipment
```

with `shipment_id` directly on `PurchaseFigurine`.

That works only if:

```text
one purchase line -> at most one shipment
```

But it fails for:

```text
Purchase line:
    Pegasus × 3

Shipment #1:
    Pegasus × 1

Shipment #2:
    Pegasus × 2
```

The fourth entity:

```text
CollectorPurchaseShipmentFigurine
```

solves that by turning the shipment relationship into an explicit quantity allocation.

---

# Recommended Final Package Structure

```text
com.mesofi.mythclothapi
│
├── common
│   └── CurrencyCode.java
│
├── collectors
│   └── Collector.java
│
├── collectorscollections
│   └── model
│       ├── CollectorCollection.java
│       └── CollectorCollectionFigurine.java
│
└── collectorspurchases
    └── model
        ├── CollectorPurchase.java
        ├── CollectorPurchaseFigurine.java
        ├── CollectorPurchaseShipment.java
        ├── CollectorPurchaseShipmentFigurine.java
        ├── PurchaseChannel.java
        ├── PurchaseType.java
        └── ShippingStatus.java
```

---

# Final Conceptual Model

The final design can be summarized as:

```text
Collector
   |
   | owns
   v
CollectorPurchase
   |
   +-----------------------+
   |                       |
   | contains              | contains
   v                       v
PurchaseFigurine        Shipment
   |                       |
   | allocated to          | contains
   |                       v
   +---------------> ShipmentFigurine
```

Where:

```text
CollectorPurchase
    = the order/transaction

CollectorPurchaseFigurine
    = what was purchased and how many

CollectorPurchaseShipment
    = how the order was physically fulfilled

CollectorPurchaseShipmentFigurine
    = how many units of a purchase line were included in a shipment
```

This model is intentionally flexible enough for:

- physical-store purchases
- online purchases
- second-hand purchases
- preorders
- one order with multiple figurines
- one order with multiple shipments
- partial shipments
- quantity splits across shipments
- different release dates
- purchases with no shipment
- purchase history tied to the collector's collection
