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
public class StockLedgerResponse {

    private List<StockLedgerRowResponse> rows;
    // Balance immediately before the first row (0 when dateFrom is unset).
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
}
