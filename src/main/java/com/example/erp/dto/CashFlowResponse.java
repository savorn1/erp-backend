package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Built from actual BankTransaction activity, not from Journal Entry — see
// FinancialReportServiceImpl's own comment on why. Transfers between two of
// the company's own accounts show up on both accounts but cancel out in the
// totals automatically (one account's outflow is another's equal inflow).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List<CashFlowAccountRowResponse> accounts;
    private BigDecimal totalOpeningBalance;
    private BigDecimal totalInflow;
    private BigDecimal totalOutflow;
    private BigDecimal totalNetChange;
    private BigDecimal totalClosingBalance;
}
