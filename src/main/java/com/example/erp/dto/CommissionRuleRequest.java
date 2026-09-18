package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CommissionRuleRequest {

    @NotNull
    private Long companyId;

    // Null means this is the company-wide default rate, applied to any
    // sales rep with no rule of their own.
    private Long userId;

    @NotNull
    @DecimalMin(value = "0", message = "ratePercent must be between 0 and 100")
    @DecimalMax(value = "100", message = "ratePercent must be between 0 and 100")
    private BigDecimal ratePercent;

    private boolean active = true;
}
