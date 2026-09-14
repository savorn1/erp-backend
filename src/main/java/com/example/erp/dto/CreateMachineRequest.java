package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateMachineRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long workCenterId;

    @NotBlank
    private String name;

    private String code;

    @DecimalMin(value = "0", message = "Cost per hour cannot be negative")
    private BigDecimal costPerHour;
}
