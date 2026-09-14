package com.example.erp.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ReconcileTransactionsRequest {

    @NotEmpty
    private List<Long> transactionIds;

    @NotNull
    private LocalDate statementDate;

    // What the bank statement says the balance is as of statementDate — the
    // response's `difference` tells the user whether marking these
    // transactions reconciled actually ties out to it.
    @NotNull
    private BigDecimal statementBalance;
}
