package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

// Used for both create and update.
@Data
public class UomConversionRequest {

    @NotNull
    private Long fromUnitOfMeasureId;

    @NotNull
    private Long toUnitOfMeasureId;

    // 1 unit of fromUnitOfMeasureId equals this many units of toUnitOfMeasureId.
    @NotNull
    @DecimalMin(value = "0.000001", message = "Conversion factor must be greater than zero")
    private BigDecimal conversionFactor;

    private boolean active = true;
}
