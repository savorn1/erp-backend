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
public class BalanceSheetResponse {

    private LocalDate asOfDate;
    private List<BalanceSheetLineResponse> assets;
    private BigDecimal assetsTotal;
    private List<BalanceSheetLineResponse> liabilities;
    private BigDecimal liabilitiesTotal;
    // Includes a synthetic "Current period earnings" line (cumulative
    // revenue - expense to date, since there's no period-closing step yet
    // that would fold it into a real retained-earnings account).
    private List<BalanceSheetLineResponse> equity;
    private BigDecimal equityTotal;
    private BigDecimal liabilitiesAndEquityTotal;
    // assetsTotal == liabilitiesAndEquityTotal — always true given the
    // synthetic earnings line, included so the frontend doesn't need to
    // recompute the comparison itself.
    private boolean balanced;
}
