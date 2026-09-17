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

// Current on-hand quantity for one product at one location (warehouse, or a
// specific bin within it). One row per (productId, warehouseId, binId) —
// found-or-created and incremented by GoodsReceiptServiceImpl; never written
// to directly from the API. No DB-level unique constraint on that triple:
// most databases (Postgres included) don't treat repeated NULLs as duplicates
// for a nullable binId, so the find-or-create in the service layer is the
// only thing guaranteeing one row per combination.
@Entity
@Table(name = "stock_levels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockLevel {

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

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal quantityOnHand = BigDecimal.ZERO;

    // Stock claimed by a confirmed Sales Order but not yet delivered — only
    // ever populated on the unbinned row per (productId, warehouseId), same
    // "unbinned row is the pool row" convention void/restore logic already
    // uses elsewhere. Written by StockAvailabilityService/SalesOrderServiceImpl/
    // DeliveryServiceImpl only when InventorySettings.reserveStock is on;
    // every availability check subtracts it from quantityOnHand to get what's
    // actually sellable. columnDefinition carries an explicit SQL default —
    // this table already has rows, and ddl-auto=update's "add column ... not
    // null" with no default fails against Postgres once any row exists (see
    // PosSaleLine.returnedQuantity for the exact same fix).
    @Column(name = "reserved_quantity", nullable = false, precision = 19, scale = 4,
            columnDefinition = "numeric(19,4) not null default 0")
    @Builder.Default
    private BigDecimal reservedQuantity = BigDecimal.ZERO;
}
