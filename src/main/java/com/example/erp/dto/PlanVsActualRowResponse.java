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
public class PlanVsActualRowResponse {

    private String moNumber;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal plannedQuantity;
    private BigDecimal producedQuantity;
    private BigDecimal scrapQuantity;
    // producedQuantity / plannedQuantity * 100.
    private BigDecimal achievementPercent;
    // producedQuantity / (producedQuantity + scrapQuantity) * 100.
    private BigDecimal yieldPercent;
}
