# Myth Cloth Purchase Model — Simplified Design

## Overview

The original purchase model became more complicated than the core needs of the Myth Cloth collection app justified.

The first design introduced four purchase-related entities:

- `CollectorPurchase`
- `CollectorPurchaseFigurine`
- `CollectorPurchaseShipment`
- `CollectorPurchaseShipmentFigurine`

That model was technically valid, but it modeled detailed logistics: multiple shipments, partial shipment quantities, and allocation of purchase-line quantities across shipments.

For the current application, that level of detail is unnecessary.

The simplified model uses:

- `CollectorPurchase`
- `CollectorPurchaseFigurine`
- `CollectorCollectionFigurine`

There is no separate shipment entity.

---

# 1. Why the model was simplified

The key question is:

> What does the application actually need the purchase feature to answer?

For the Myth Cloth collection app, the important questions are primarily:

1. What did I buy?
2. When did I buy it?
3. From whom?
4. How many did I buy?
5. How much did I pay?
6. Was it retail, preorder, or second-hand?
7. Was the purchase online or from a physical store?
8. If it was online, has it shipped/delivered?
9. Which figurine in my collection does this purchase relate to?

Those questions do not require a full shipment-allocation model.

The original model was effectively designed to answer a much more detailed question:

> "Exactly which quantities from each purchase line were placed into which physical shipment?"

That is useful for sophisticated order-management software, but it is probably unnecessary for a personal collection tracker.

---

# 2. Simplified domain model

```text
Collector
   │
   ├── * CollectorCollection
   │          │
   │          └── * CollectorCollectionFigurine
   │
   └── * CollectorPurchase
              │
              └── * CollectorPurchaseFigurine
                           │
                           └── CollectorCollectionFigurine
```

The important relationship is:

```text
CollectorPurchaseFigurine
        │
        └── CollectorCollectionFigurine
```

A purchase line points to the collector's collection figurine.

This means purchase history is associated with the collector's actual collection rather than only with the global catalog `Figurine`.

---

# 3. The three important concepts

## 3.1 CollectorCollectionFigurine

This represents the collector's current relationship with a catalog figurine.

Example:

```text
Collection: My Saint Seiya Collection

Pegasus Seiya
quantity = 3
owned = true
condition = NEW
```

The important concept is:

> This represents what the collector currently has in a collection.

It is not a purchase record.

---

## 3.2 CollectorPurchase

This represents an overall purchase/order.

Example:

```text
Purchase
--------------------------------
Date:       2026-09-10
Seller:     Mandarake
Order #:    ABC-123
Currency:   JPY
Total:      ¥35,000
Channel:    ONLINE
Status:     DELIVERED
Carrier:    DHL
Tracking:   123456789
```

A physical-store purchase can simply have no shipping information:

```text
Purchase
--------------------------------
Date:       2026-09-10
Seller:     Local Hobby Shop
Currency:   MXN
Total:      $8,500
Channel:    PHYSICAL_STORE
Shipping:   null
```

There is no need for an empty shipment object.

---

## 3.3 CollectorPurchaseFigurine

This represents one figurine line within a purchase.

Example:

```text
Purchase #123
    └── Pegasus Seiya
        quantity = 2
        pricePaid = ¥12,000
        purchaseType = SECOND_HAND
```

The purchase line points to the corresponding `CollectorCollectionFigurine`.

This allows the application to answer:

> "How did this collection item get acquired?"

---

# 4. Recommended CollectorPurchase

```java
@Entity
@Getter
@Setter
@Table(name = "collector_purchases")
public class CollectorPurchase extends Auditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collector_id", nullable = false)
    private Collector collector;

    @Column(nullable = false)
    private LocalDate orderDate;

    @Column(length = 150)
    private String seller;

    @Column(length = 50)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private CurrencyCode currency;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseChannel purchaseChannel;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ShippingStatus shippingStatus;

    @Column(length = 100)
    private String trackingNumber;

    @Column(length = 100)
    private String carrier;

    private LocalDate shippedDate;

    private LocalDate deliveredDate;

    @OneToMany(
        mappedBy = "purchase",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<CollectorPurchaseFigurine> figurines = new ArrayList<>();
}
```

The purchase stores order-level information:

- collector
- order date
- seller
- order number
- currency
- total amount
- purchase channel
- shipping status
- tracking information

Figurine-specific information belongs in `CollectorPurchaseFigurine`.

---

# 5. Recommended CollectorPurchaseFigurine

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
}
```

## Meaning of `pricePaid`

The recommended meaning is:

> Unit price paid for one figurine.

For example:

```text
quantity  = 3
pricePaid = ¥15,000
```

means:

```text
3 × ¥15,000 = ¥45,000
```

If you instead want `pricePaid` to represent the complete line total, rename it to:

```java
private BigDecimal lineAmount;
```

Keeping `pricePaid` as the unit price makes the meaning straightforward.

---

# 6. Do not add the purchase collection to CollectorCollectionFigurine

One important change from the previous design is to **not** add:

```java
@OneToMany(
    mappedBy = "collectionFigurine",
    cascade = CascadeType.ALL,
    orphanRemoval = true
)
private List<CollectorPurchaseFigurine> purchaseFigurines;
```

The relationship can exist perfectly well with only this on `CollectorPurchaseFigurine`:

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "collection_figurine_id", nullable = false)
private CollectorCollectionFigurine collectionFigurine;
```

This keeps `CollectorCollectionFigurine` focused on its primary responsibility:

> representing the figurine in the collector's collection.

Purchase history can be queried through a repository when needed:

```java
List<CollectorPurchaseFigurine>
findByCollectionFigurineId(Long collectionFigurineId);
```

This avoids making the entity graph bidirectional everywhere.

---

# 7. Collection quantity vs purchase quantity

This distinction is important.

## Collection quantity

```text
CollectorCollectionFigurine.quantity
```

means:

> How many of this figurine the collector currently has.

## Purchase quantity

```text
CollectorPurchaseFigurine.quantity
```

means:

> How many of this figurine were acquired in this purchase.

These values do not necessarily have to remain equal.

### Example

The collector buys:

```text
Purchase #1
Pegasus Seiya
quantity = 3
```

Their collection becomes:

```text
Pegasus Seiya
quantity = 3
```

Later they sell one:

```text
Pegasus Seiya
quantity = 2
```

The purchase history should remain:

```text
Purchase #1
Pegasus Seiya
quantity = 3
```

The purchase is historical; the collection is current state.

---

# 8. Example with multiple purchases

Suppose the collector has:

```text
Collection
└── Pegasus Seiya
    quantity = 3
```

The acquisition history could be:

```text
Purchase #1
    Mandarake
    Pegasus Seiya
    quantity = 2
    pricePaid = ¥12,000
    purchaseType = SECOND_HAND

Purchase #2
    Nin-Nin Game
    Pegasus Seiya
    quantity = 1
    pricePaid = ¥15,000
    purchaseType = RETAIL
```

The relationship is:

```text
CollectorCollection
    │
    └── CollectorCollectionFigurine
              │
              │ Pegasus Seiya
              │ current quantity = 3
              │
              ├── CollectorPurchaseFigurine
              │       quantity = 2
              │       purchase #1
              │
              └── CollectorPurchaseFigurine
                      quantity = 1
                      purchase #2
```

This is enough to build an acquisition-history screen.

---

# 9. Why shipments were removed

The previous model had:

```text
CollectorPurchase
    │
    ├── CollectorPurchaseFigurine
    │
    └── CollectorPurchaseShipment
              │
              └── CollectorPurchaseShipmentFigurine
```

This allowed:

```text
Purchase
5 × Pegasus Seiya

Shipment #1
2 × Pegasus Seiya

Shipment #2
3 × Pegasus Seiya
```

That is a valid model, but it introduces another entity and another association table solely to represent logistics.

For this application, it is simpler to represent:

```text
Purchase
5 × Pegasus Seiya
shippingStatus = DELIVERED
```

If a package arrives in two shipments, the application does not currently need to know that distinction.

---

# 10. If multiple shipments become important later

The simplified model does not prevent future extension.

If the application eventually needs:

- multiple packages
- partial deliveries
- different tracking numbers
- different carriers
- shipment-specific quantities
- package dates

then `CollectorPurchaseShipment` can be introduced later and, if necessary, `CollectorPurchaseShipmentFigurine`.

The core purchase model does not need to be designed around that possibility today.

A useful principle here is:

> Model the business rules you actually have, rather than every possible future scenario.

---

# 11. Why there is no reverse relationship from Collector

The purchase already has:

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "collector_id", nullable = false)
private Collector collector;
```

That is enough.

There is no immediate need for:

```java
@OneToMany
private List<CollectorPurchase> purchases;
```

on `Collector`.

If the application needs a collector's purchases, use a repository:

```java
List<CollectorPurchase> findByCollectorId(Long collectorId);
```

This avoids making `Collector` responsible for maintaining another potentially large collection.

---

# 12. Cascade and orphan removal

The simplified model has a clear ownership boundary.

## CollectorPurchase owns its purchase lines

```java
@OneToMany(
    mappedBy = "purchase",
    cascade = CascadeType.ALL,
    orphanRemoval = true
)
private List<CollectorPurchaseFigurine> figurines;
```

This means the purchase and its lines form a composition.

If a purchase line is removed from a purchase, that purchase line can be deleted.

## Purchase line does not own the purchase

Do not put:

```java
cascade = CascadeType.ALL
```

on:

```java
@ManyToOne
private CollectorPurchase purchase;
```

The purchase should not be deleted because a child happens to be removed.

---

# 13. Important deletion rule

JPA will not automatically enforce:

> A purchase should probably not exist without at least one purchase figurine.

For example, removing the final line from:

```text
Purchase #123
    └── Pegasus Seiya
```

does not automatically delete `Purchase #123`.

`orphanRemoval` handles the child entity, not the parent.

Therefore, the service should prevent or clean up an empty purchase.

For example:

```java
if (purchase.getFigurines().isEmpty()) {
    purchaseRepository.delete(purchase);
}
```

Whether the API deletes the empty purchase or rejects the operation depends on the desired behavior.

---

# 14. Validation that still belongs in the service

At minimum:

### Purchase

```text
orderDate <= today
```

### Purchase line

```text
quantity > 0
pricePaid >= 0
```

### Purchase relationship

The `collectionFigurine` must belong to the same collector as the purchase.

For example, this must be rejected:

```text
Collector A
    Purchase #1
        Pegasus Seiya belonging to Collector B
```

The database relationship alone does not prevent this. The service should verify it.

---

# 15. Recommended package structure

A simple structure can remain:

```text
collectors/
    Collector.java

collectorscollections/
    CollectorCollection.java
    model/
        CollectorCollectionFigurine.java

collectorspurchases/
    CollectorPurchase.java
    model/
        CollectorPurchaseFigurine.java
```

There is no need for a shipment package right now.

---

# 16. Final entity relationship

```text
                         Collector
                        /                                /                                 ▼             ▼
          CollectorCollection   CollectorPurchase
                    │                  │
                    │                  │
                    ▼                  ▼
       CollectorCollectionFigurine  CollectorPurchaseFigurine
                    ▲                  │
                    │                  │
                    └──────────────────┘
```

More precisely:

```text
Collector
   │
   ├── 1 → * CollectorCollection
   │              │
   │              └── 1 → * CollectorCollectionFigurine
   │
   └── 1 → * CollectorPurchase
                  │
                  └── 1 → * CollectorPurchaseFigurine
                                   │
                                   └── * → 1 CollectorCollectionFigurine
```

---

# 17. Final recommendation

Use this simplified model as the baseline:

### Entities

```text
Collector
CollectorCollection
CollectorCollectionFigurine
CollectorPurchase
CollectorPurchaseFigurine
```

### Do not add yet

```text
CollectorPurchaseShipment
CollectorPurchaseShipmentFigurine
```

### Responsibilities

| Entity | Responsibility |
|---|---|
| `Collector` | The owner/user |
| `CollectorCollection` | A custom collection |
| `CollectorCollectionFigurine` | Current collection state |
| `CollectorPurchase` | Historical order/purchase |
| `CollectorPurchaseFigurine` | Figurines acquired in that purchase |

The most important conceptual separation is:

```text
Collection = CURRENT STATE

Purchase = HISTORICAL EVENT
```

This gives you a model that is much easier to reason about while still supporting purchase history, sellers, prices, quantities, online/physical purchases, preorder information, and basic shipping tracking.

If detailed logistics become necessary later, shipments can be added without redesigning the fundamental collection/purchase relationship.
