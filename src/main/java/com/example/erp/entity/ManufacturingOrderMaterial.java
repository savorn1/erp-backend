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

@Entity
@Table(name = "manufacturing_order_materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManufacturingOrderMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "manufacturing_order_id", nullable = false)
    private Long manufacturingOrderId;

    @Column(name = "component_product_id", nullable = false)
    private Long componentProductId;

    // BillOfMaterialLine.quantity (plus its scrapPercent) scaled to this
    // order's plannedQuantity — computed once at order creation.
    @Column(name = "required_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal requiredQuantity;

    // Set to requiredQuantity when the order starts production — this
    // codebase has no partial-consumption/backflush workflow, materials are
    // drawn in full at once (see ManufacturingOrderServiceImpl.startProduction).
    @Column(name = "consumed_quantity", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal consumedQuantity = BigDecimal.ZERO;

    // Product.costPrice snapshotted at order-creation time, so a later price
    // change doesn't retroactively change an already-planned order's cost.
    @Column(name = "unit_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitCost;

    // consumedQuantity * unitCost, stamped when materials are consumed.
    @Column(name = "line_cost", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal lineCost = BigDecimal.ZERO;
}
