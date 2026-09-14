package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Uses Product.costPrice/sellingPrice as a static cost basis — this codebase
// has no FIFO/average-cost layering, so "profitability" here means "at
// today's list price/cost", not a historical actual-cost calculation.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockProfitabilityRowResponse {

    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal quantitySold;
    private BigDecimal revenue;
    private BigDecimal cogs;
    private BigDecimal grossProfit;
    private BigDecimal marginPercent;
    private BigDecimal currentStockValue;
    // cogs / currentStockValue — a simple inventory turnover proxy; null
    // when currentStockValue is zero.
    private BigDecimal turnoverRatio;
}
