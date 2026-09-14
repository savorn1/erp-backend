package com.example.erp.dto;

import com.example.erp.entity.BalanceAdjustmentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BalanceAdjustmentRequest {

    @NotNull
    private BalanceAdjustmentType type;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String note;
}
