package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class JournalEntryLineRequest {

    @NotNull
    private Long accountId;

    @NotNull
    @DecimalMin(value = "0", message = "Debit cannot be negative")
    private BigDecimal debit = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(value = "0", message = "Credit cannot be negative")
    private BigDecimal credit = BigDecimal.ZERO;

    private String description;
}
