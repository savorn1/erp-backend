package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

// Finds-or-updates by (companyId, accountId, costCenterId, accountingPeriodId)
// — see BudgetServiceImpl.upsertBudget. Re-submitting the same combination
// edits that row in place rather than creating a duplicate.
@Data
public class UpsertBudgetRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long accountId;

    // Optional — null means "not scoped to a cost center."
    private Long costCenterId;

    @NotNull
    private Long accountingPeriodId;

    @NotNull
    private BigDecimal amount;

    private String notes;
}
