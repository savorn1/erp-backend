package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateManufacturingOrderRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long bomId;

    @NotNull
    private Long warehouseId;

    // Optional — groups this order under a ProductionPlan.
    private Long productionPlanId;

    @NotNull
    @DecimalMin(value = "0.0001", message = "Planned quantity must be greater than zero")
    private BigDecimal plannedQuantity;

    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private String notes;
}
