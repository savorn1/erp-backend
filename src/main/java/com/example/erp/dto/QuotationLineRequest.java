package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuotationLineRequest {

    @NotNull
    private Long productId;

    @NotNull
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    @NotNull
    @DecimalMin(value = "0", message = "Unit price cannot be negative")
    private BigDecimal unitPrice;
}
