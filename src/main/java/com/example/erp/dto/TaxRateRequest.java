package com.example.erp.dto;

import com.example.erp.entity.TaxType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

// Used for both create and update.
@Data
public class TaxRateRequest {

    @NotNull
    private Long companyId;

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotNull
    private TaxType type;

    @NotNull
    @DecimalMin(value = "0", message = "Rate cannot be negative")
    private BigDecimal ratePercent;

    private boolean active = true;
}
