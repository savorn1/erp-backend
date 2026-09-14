package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BankTransferRequest {

    @NotNull
    private Long toBankAccountId;

    @NotNull
    private LocalDate transactionDate;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String reference;

    private String description;
}
