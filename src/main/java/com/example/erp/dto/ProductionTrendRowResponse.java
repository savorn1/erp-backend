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
public class ProductionTrendRowResponse {

    // "yyyy-MM".
    private String month;
    private long orderCount;
    private BigDecimal totalProducedQuantity;
    private BigDecimal totalScrapQuantity;
}
