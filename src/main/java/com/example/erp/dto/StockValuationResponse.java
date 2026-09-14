package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

// Grouped by warehouse — see InventoryReportServiceImpl.stockValuation.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockValuationResponse {

    private List<StockValuationRowResponse> rows;
    private BigDecimal totalQuantity;
    private BigDecimal totalValue;
}
