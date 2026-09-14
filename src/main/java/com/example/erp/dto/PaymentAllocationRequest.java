package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentAllocationRequest {

    @NotNull
    private Long invoiceId;

    @NotNull
    @DecimalMin(value = "0.01", message = "Allocation amount must be greater than zero")
    private BigDecimal amount;
}
