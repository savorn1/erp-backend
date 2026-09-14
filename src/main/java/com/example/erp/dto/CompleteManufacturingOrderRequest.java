package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CompleteManufacturingOrderRequest {

    @NotNull
    @DecimalMin(value = "0", message = "Produced quantity cannot be negative")
    private BigDecimal producedQuantity;

    @DecimalMin(value = "0", message = "Scrap quantity cannot be negative")
    private BigDecimal scrapQuantity;

    private String scrapReason;

    @DecimalMin(value = "0", message = "Labor cost cannot be negative")
    private BigDecimal laborCost;

    @DecimalMin(value = "0", message = "Overhead cost cannot be negative")
    private BigDecimal overheadCost;
}
