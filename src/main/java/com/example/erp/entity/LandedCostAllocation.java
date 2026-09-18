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

// One line's share of a LandedCost — an immutable audit record of exactly
// what was applied to a GoodsReceiptLine/Product at allocation time, even
// though Product.costPrice can later change again (another receipt, a
// manual edit, another landed cost). Thin junction entity, no logic of its
// own — mirrors PaymentAllocation's shape.
@Entity
@Table(name = "landed_cost_allocations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandedCostAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "landed_cost_id", nullable = false)
    private Long landedCostId;

    @Column(name = "goods_receipt_line_id", nullable = false)
    private Long goodsReceiptLineId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    // The base-UOM quantity this allocation's ratio was computed against.
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    // Null when the receipt line had no unitCost snapshot at allocation time.
    @Column(name = "original_unit_cost", precision = 19, scale = 4)
    private BigDecimal originalUnitCost;

    @Column(name = "allocated_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal allocatedAmount;

    @Column(name = "new_unit_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal newUnitCost;
}
