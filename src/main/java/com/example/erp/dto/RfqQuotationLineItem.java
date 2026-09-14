package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RfqQuotationLineItem {

    @NotNull
    private Long productId;

    @NotNull
    @DecimalMin(value = "0", message = "Unit price cannot be negative")
    private BigDecimal unitPrice;
}
