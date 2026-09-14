package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesOrderLineRequest {

    @NotNull
    private Long productId;

    @NotNull
    @DecimalMin(value = "0.0001", message = "Quantity ordered must be greater than zero")
    private BigDecimal quantityOrdered;

    // Defaults to the product's own sellingPrice when omitted — see
    // SalesOrderServiceImpl.saveLines.
    @DecimalMin(value = "0", message = "Unit price cannot be negative")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0", message = "Discount cannot be negative")
    @DecimalMax(value = "100", message = "Discount cannot exceed 100%")
    private BigDecimal discountPercent;

    // Defaults to the product's own taxRate when omitted.
    @DecimalMin(value = "0", message = "Tax rate cannot be negative")
    private BigDecimal taxRate;
}
