package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// Shared body for deposit/withdraw — direction comes from which endpoint is
// called (see BankAccountController).
@Data
public class BankTransactionRequest {

    @NotNull
    private LocalDate transactionDate;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String reference;

    private String description;
}
