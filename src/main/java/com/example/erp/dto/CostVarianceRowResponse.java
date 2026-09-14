package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostVarianceRowResponse {

    private String moNumber;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal plannedQuantity;
    // BOM's current material-cost-per-unit * plannedQuantity.
    private BigDecimal standardMaterialCost;
    // The order's actual stamped materialCost.
    private BigDecimal actualMaterialCost;
    // actual - standard. Positive means it cost more than the recipe implied.
    private BigDecimal varianceAmount;
    private BigDecimal variancePercent;
}
