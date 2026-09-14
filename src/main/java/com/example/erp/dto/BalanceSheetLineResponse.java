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
public class BalanceSheetLineResponse {

    // Null on the synthetic "Current period earnings" equity line — see
    // FinancialReportServiceImpl.balanceSheet.
    private Long accountId;
    private String accountCode;
    private String accountName;
    private BigDecimal amount;
}
