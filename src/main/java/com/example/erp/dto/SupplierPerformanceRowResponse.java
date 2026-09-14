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
}
