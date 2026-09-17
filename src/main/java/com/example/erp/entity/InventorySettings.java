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

// One row per company — the inventory policy consulted by StockAvailabilityService
// before POS checkout/exchange and Sales Order confirmation. Same "one row per
// company, enforced in the service layer, not a DB constraint" convention as
// PostingRule. Stock Transfers, Stock Adjustments, and Manufacturing Order
// consumption never consult this — those are internal movements, not sales,
// and keep their existing hard "insufficient stock" blocks unconditionally.
@Entity
@Table(name = "inventory_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventorySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    // Lets a sale/order exceed *available* stock (on-hand minus any active
    // reservation) at all. False means any shortfall against available is a
    // hard block, exactly like every other stock-decrementing flow in this
    // codebase today.
    @Column(name = "allow_overselling", nullable = false)
    @Builder.Default
    private boolean allowOverselling = false;

    // Lets a sale/order push actual on-hand quantity below zero. Independent
    // of allowOverselling: overselling can still be fully covered by physical
    // stock if it's only eating into someone else's reservation buffer.
    @Column(name = "allow_negative_stock", nullable = false)
    @Builder.Default
    private boolean allowNegativeStock = false;

    // Frontend-only display toggle — whether POS/Sales Order line entry shows
    // an available-stock count/badge to the person entering the line. Never
    // affects server-side enforcement either way.
    @Column(name = "show_available_stock", nullable = false)
    @Builder.Default
    private boolean showAvailableStock = true;

    // Whether confirming a Sales Order persists a real reservation
    // (StockLevel.reservedQuantity) that POS/other Sales Orders must then
    // respect. False means "available" always equals raw on-hand, same as
    // this codebase's behavior before this feature existed.
    @Column(name = "reserve_stock", nullable = false)
    @Builder.Default
    private boolean reserveStock = true;

    // When an oversold sale/order is permitted, lets it proceed for the full
    // requested quantity with the physical shortfall recorded as a
    // backordered quantity on the line, instead of only ever letting through
    // exactly what's on hand.
    @Column(name = "backorder_enabled", nullable = false)
    @Builder.Default
    private boolean backorderEnabled = false;

    // Only meaningful when allowOverselling is true — requires the acting
    // user to hold Role.ADMIN to actually go through with an oversold
    // sale/order; a USER-role account is blocked with a clear message.
    @Column(name = "overselling_approval_required", nullable = false)
    @Builder.Default
    private boolean oversellingApprovalRequired = false;

    // Frontend-only display toggle — whether the dashboard's "Low stock
    // items" tile and POS's amber low-stock badge treatment show at all
    // (the per-product Product.reorderPoint threshold still governs *which*
    // products would qualify either way).
    @Column(name = "stock_warning_enabled", nullable = false)
    @Builder.Default
    private boolean stockWarningEnabled = true;
}
