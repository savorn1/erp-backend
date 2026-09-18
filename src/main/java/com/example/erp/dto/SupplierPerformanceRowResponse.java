package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierPerformanceRowResponse {

    private Long supplierId;
    private String supplierName;
    private long orderCount;
    // Orders with at least one goods receipt and a known expected date —
    // the only ones a delivery-timeliness figure can be computed for.
    private long measurableOrderCount;
    private long onTimeOrderCount;
    // Null when measurableOrderCount is zero.
    private Double onTimePercent;
    // Average of (latest receipt date - expected date) across measurable
    // orders — the latest receipt stands in for "fully received"; positive
    // means late, negative means early. Null when measurableOrderCount is zero.
    private Double averageDelayDays;
    // PASSED / (PASSED + FAILED) across this supplier's quality-checked
    // goods-receipt lines. Null when none of their lines have been checked yet.
    private Double qualityPassPercent;
    // Quantity-weighted average % deviation from the cross-supplier price
    // benchmark, across only the products this supplier shares with at least
    // one other supplier in the filtered period. Positive means they charge
    // above the market average, negative below. Null when none of their
    // products have a comparable benchmark.
    private Double priceVariancePercent;
}
