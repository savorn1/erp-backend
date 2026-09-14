package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// Only allowed while the order is still DRAFT (see
// ManufacturingOrderServiceImpl.updateOrder). The BOM is deliberately not
// editable — swapping BOMs mid-plan would require re-deriving the whole
// material list against a different recipe; cancel and re-create instead.
// Changing plannedQuantity regenerates the material requirement lines.
@Data
public class UpdateManufacturingOrderRequest {

    @NotNull
    private Long warehouseId;

    private Long productionPlanId;

    @NotNull
    @DecimalMin(value = "0.0001", message = "Planned quantity must be greater than zero")
    private BigDecimal plannedQuantity;

    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private String notes;
}
