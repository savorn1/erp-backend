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
import java.time.LocalDate;

// batchNumber/serialNumbersRaw are the requester's raw, unresolved input —
// deliberately not turned into a real ProductBatch/SerialNumber row (or a
// StockLevel change) until the adjustment is approved, so a pending request
// has zero effect on stock. batchId is filled in by the approval itself.
@Entity
@Table(name = "stock_adjustment_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAdjustmentLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_adjustment_id", nullable = false)
    private Long stockAdjustmentId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "bin_id")
    private Long binId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockAdjustmentReason reason;

    // Always a positive magnitude — the reason determines the sign applied
    // to StockLevel at approval time.
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    // Required when the product is BATCH-tracked. For STOCK_INCREASE this may
    // be a brand-new batch number; for every other reason it must already
    // exist at approval time.
    private String batchNumber;

    // Only meaningful for STOCK_INCREASE on a BATCH-tracked product that
    // doesn't already exist — mirrors GoodsReceiptLine's own field.
    private LocalDate expirationDate;

    // Required when the product is SERIAL-tracked — newline/comma separated,
    // parsed and validated at approval time (new units for STOCK_INCREASE,
    // existing IN_STOCK units otherwise).
    @Column(columnDefinition = "text")
    private String serialNumbersRaw;

    // Resolved at approval time.
    @Column(name = "batch_id")
    private Long batchId;
}
