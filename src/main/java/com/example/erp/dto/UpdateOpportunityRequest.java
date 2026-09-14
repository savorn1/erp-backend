package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateOpportunityRequest {

    @NotNull
    private Long companyId;

    @NotBlank
    private String name;

    @DecimalMin(value = "0", message = "Amount cannot be negative")
    private BigDecimal amount;

    @Min(0)
    @Max(100)
    private Integer probability;

    private LocalDate expectedCloseDate;

    private Long assignedToUserId;

    private String notes;
}
