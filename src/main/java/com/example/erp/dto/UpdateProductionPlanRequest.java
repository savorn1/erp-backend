package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

// Only allowed while the plan is still DRAFT (see ProductionPlanServiceImpl).
@Data
public class UpdateProductionPlanRequest {

    @NotBlank
    private String name;

    @NotNull
    private LocalDate periodStart;

    @NotNull
    private LocalDate periodEnd;

    private String notes;
}
