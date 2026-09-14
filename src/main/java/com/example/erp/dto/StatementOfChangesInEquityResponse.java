package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementOfChangesInEquityResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Beginning equity, Net income, Other equity changes, Ending equity —
    // in that order, ready to render as a simple statement table.
    private List<StatementOfChangesInEquityLineResponse> lines;
    private BigDecimal beginningEquity;
    private BigDecimal netIncome;
    // Residual: (endingEquity - beginningEquity) - netIncome. Represents
    // capital contributions, owner draws, or any other direct posting to an
    // EQUITY account during the period that isn't revenue/expense.
    private BigDecimal otherEquityChanges;
    private BigDecimal endingEquity;
}
