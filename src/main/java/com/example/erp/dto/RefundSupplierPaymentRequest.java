package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RefundSupplierPaymentRequest {

    @NotNull
    private LocalDate refundDate;

    @NotNull
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than zero")
    private BigDecimal amount;

    private String reason;
}
