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
public class MoSummaryRowResponse {

    private String status;
    private long orderCount;
    private BigDecimal totalPlannedQuantity;
    private BigDecimal totalProducedQuantity;
    private BigDecimal totalScrapQuantity;
}
