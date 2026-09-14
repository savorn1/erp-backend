package com.example.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Plain FK columns (not JPA relations) — matches this codebase's existing
    // convention (see RefreshToken.userId, Branch.companyId). categoryId/
    // brandId/typeId/supplierId are optional classification; unitOfMeasureId
    // is required — every sellable product needs one.
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "type_id")
    private Long typeId;

    @Column(name = "unit_of_measure_id", nullable = false)
    private Long unitOfMeasureId;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private String sku;

    private String barcode;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal costPrice;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal sellingPrice;

    // Percent, e.g. 10.00 = 10%.
    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    // Deliberately nullable (unlike status/taxRate above) even though new
    // rows always get NONE via the builder default — a NOT NULL column here
    // would fail to add itself via ddl-auto=update once any product rows
    // already exist. Treat a null read as NONE (see ProductServiceImpl).
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProductTrackingType trackingType = ProductTrackingType.NONE;

    private String imageUrl;

    // Threshold for the Low Stock report (see InventoryReportServiceImpl) —
    // a product is never flagged when this is null or zero, meaning "no
    // threshold configured" rather than "reorder immediately". Nullable for
    // the same ddl-auto=update reason as trackingType above; treat a null
    // read as zero.
    @Column(name = "reorder_point", precision = 19, scale = 4)
    private BigDecimal reorderPoint;

    // Threshold for the Overstock report (see InventoryReportServiceImpl) —
    // a product is never flagged when this is null or zero, meaning "no
    // threshold configured". Same nullable-for-ddl-auto=update reasoning as
    // reorderPoint above; treat a null read as zero.
    @Column(name = "max_stock", precision = 19, scale = 4)
    private BigDecimal maxStock;
}
