package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceGroupRequest {

    @NotBlank
    private String name;

    @DecimalMin(value = "0", message = "discountPercent must be between 0 and 100")
    @DecimalMax(value = "100", message = "discountPercent must be between 0 and 100")
    private BigDecimal discountPercent;

    private boolean active = true;
}
