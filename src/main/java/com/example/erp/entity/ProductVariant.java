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

// A sellable variation of a Product (e.g. "Red / Large") with its own SKU/
// barcode and optional price/unit overrides. costPrice/sellingPrice/
// unitOfMeasureId being null means "inherit the parent product's own value"
// — resolved by the caller, not stored redundantly here.
@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String sku;

    private String barcode;

    // Optional override — null means this variant uses the parent product's
    // own unitOfMeasureId.
    @Column(name = "unit_of_measure_id")
    private Long unitOfMeasureId;

    @Column(precision = 19, scale = 4)
    private BigDecimal costPrice;

    @Column(precision = 19, scale = 4)
    private BigDecimal sellingPrice;

    private String imageUrl;

    @Builder.Default
    private boolean active = true;
}
