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

// The price a product sells for under a given PriceGroup — at most one row
// per (productId, priceGroupId), enforced in ProductPriceServiceImpl rather
// than a DB constraint (matches this codebase's existing lookup-uniqueness
// convention, e.g. CustomerGroupServiceImpl's own name check). Falls back to
// Product.sellingPrice when no row exists for the customer's price group —
// see SalesOrderServiceImpl.
@Entity
@Table(name = "product_prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "price_group_id", nullable = false)
    private Long priceGroupId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;
}
