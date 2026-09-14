package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillOfMaterialLineRequest {

    @NotNull
    private Long componentProductId;

    @NotNull
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    @DecimalMin(value = "0", message = "Scrap percent cannot be negative")
    @DecimalMax(value = "100", message = "Scrap percent cannot exceed 100%")
    private BigDecimal scrapPercent;
}
