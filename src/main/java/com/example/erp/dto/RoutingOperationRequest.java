package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoutingOperationRequest {

    @NotNull
    private Integer sequenceNumber;

    @NotBlank
    private String name;

    @NotNull
    private Long workCenterId;

    private Long machineId;

    @DecimalMin(value = "0", message = "Standard time cannot be negative")
    private BigDecimal standardTimeMinutes;
}
