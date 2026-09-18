package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecurringInvoiceTemplateLineRequest {

    @NotNull
    private Long productId;

    @NotNull
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    // Omit to default to the product's own selling price/tax rate at
    // generation time.
    private BigDecimal unitPrice;
    private BigDecimal discountPercent;
    private BigDecimal taxRate;
}
