package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateWorkCenterRequest {

    @NotNull
    private Long companyId;

    @NotBlank
    private String name;

    private String code;
    private Long warehouseId;
    private String description;

    @DecimalMin(value = "0", message = "Capacity cannot be negative")
    private BigDecimal capacityPerHour;
}
