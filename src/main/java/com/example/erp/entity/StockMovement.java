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
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// An append-only audit trail of every stock quantity change ("Stock
// Increase" and, in later steps, decreases) — one row per product/location
// per movement, written by GoodsReceiptServiceImpl alongside the StockLevel
// update it reflects.
@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "bin_id")
    private Long binId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockMovementType type;

    // Signed — positive for a receipt. Future movement types (issue,
    // transfer-out) would use a negative delta.
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityDelta;

    // What caused this movement, e.g. "GOODS_RECEIPT" + the GoodsReceipt id.
    @Column(nullable = false)
    private String referenceType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
