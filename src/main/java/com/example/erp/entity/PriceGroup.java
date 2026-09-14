package com.example.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// A pricing tier (e.g. Retail/Wholesale/Distributor/VIP) — CustomerGroup.priceGroupId
// links each customer group to one, and ProductPrice carries the actual
// per-product price for that tier (see SalesOrderServiceImpl's pricing
// cascade: Customer -> CustomerGroup -> PriceGroup -> ProductPrice).
@Entity
@Table(name = "price_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Default markdown off a product's sellingPrice for this tier (e.g. 15.00
    // = 15% off), applied by SalesOrderServiceImpl.resolveUnitPrice when no
    // per-product ProductPrice override exists. Null means no automatic
    // discount — only explicit ProductPrice rows apply.
    @Column(name = "discount_percent")
    private BigDecimal discountPercent;

    @Builder.Default
    private boolean active = true;
}
