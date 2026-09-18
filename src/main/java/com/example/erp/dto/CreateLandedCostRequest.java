package com.example.erp.dto;

import com.example.erp.entity.LandedCostAllocationMethod;
import com.example.erp.entity.LandedCostType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateLandedCostRequest {

    @NotNull
    private Long goodsReceiptId;

    @NotNull
    private LandedCostType costType;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotNull
    private LandedCostAllocationMethod allocationMethod;

    @NotNull
    private LocalDate costDate;

    private String reference;

    private String notes;
}
