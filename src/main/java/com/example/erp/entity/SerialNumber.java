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

import java.time.LocalDateTime;

// One individually-tracked unit of a SERIAL-tracked product, created when a
// goods receipt line supplies its serial numbers (one per unit received).
@Entity
@Table(name = "serial_numbers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SerialNumber {

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

    @Column(name = "batch_id")
    private Long batchId;

    // Globally unique, not just per-product — matches how serial numbers work
    // in the real world (the manufacturer's own identifier).
    @Column(nullable = false, unique = true)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SerialNumberStatus status = SerialNumberStatus.IN_STOCK;

    // Nullable — a unit can instead originate from an approved StockAdjustment
    // (STOCK_INCREASE) via adjustmentLineId below.
    @Column(name = "goods_receipt_line_id")
    private Long goodsReceiptLineId;

    // Set when a delivery ships this unit out (see DeliveryServiceImpl).
    @Column(name = "delivery_line_id")
    private Long deliveryLineId;

    // Set while this unit is attached to an in-flight stock transfer (from
    // ship through receive — see StockTransferServiceImpl); cleared once
    // received back to null.
    @Column(name = "transfer_line_id")
    private Long transferLineId;

    // Set when an approved StockAdjustment creates (STOCK_INCREASE) or
    // removes (all other reasons) this unit — see StockAdjustmentServiceImpl.
    @Column(name = "adjustment_line_id")
    private Long adjustmentLineId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
