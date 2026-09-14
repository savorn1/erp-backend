package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockProfitabilityResponse {

    private List<StockProfitabilityRowResponse> rows;
    private BigDecimal totalRevenue;
    private BigDecimal totalCogs;
    private BigDecimal totalGrossProfit;
}
