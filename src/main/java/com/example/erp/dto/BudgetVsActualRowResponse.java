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
public class BudgetVsActualRowResponse {

    private Long accountId;
    private String accountCode;
    private String accountName;
    private String accountType;
    private BigDecimal budgetAmount;
    private BigDecimal actualAmount;
    // actualAmount - budgetAmount, both already sign-normalized to the
    // account's own debit/credit-normal direction — a positive variance
    // always means "actual ran ahead of budget" regardless of account type.
    private BigDecimal varianceAmount;
}
