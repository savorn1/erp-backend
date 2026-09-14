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
import java.time.LocalDate;

// The selling price for one ProductUom row under one PriceGroup — e.g.
// Coca-Cola 330ml's BOX unit priced at $12.00 Retail / $10.50 Wholesale. At
// most one row per (productUomId, priceGroupId), enforced in
// ProductUomPriceServiceImpl rather than a DB constraint (matches this
// codebase's existing lookup-uniqueness convention, e.g. ProductPrice's own
// per-(product, priceGroup) rule). effectiveFrom/effectiveTo are optional —
// null on either side means no start/end bound.
@Entity
@Table(name = "product_uom_prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUomPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_uom_id", nullable = false)
    private Long productUomId;

    @Column(name = "price_group_id", nullable = false)
    private Long priceGroupId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Builder.Default
    private boolean active = true;
}
