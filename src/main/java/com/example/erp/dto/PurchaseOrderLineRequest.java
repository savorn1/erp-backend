package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseOrderLineRequest {

    @NotNull
    private Long productId;

    // Optional — omit to order in the product's own base unit. When set,
    // must be either the product's base unit or a unit already registered
    // (with allowPurchase) as a ProductUom for this product.
    private Long unitOfMeasureId;

    @NotNull
    @DecimalMin(value = "0.0001", message = "Quantity ordered must be greater than zero")
    private BigDecimal quantityOrdered;

    @NotNull
    @DecimalMin(value = "0", message = "Unit cost cannot be negative")
    private BigDecimal unitCost;

    @DecimalMin(value = "0", message = "Discount cannot be negative")
    @DecimalMax(value = "100", message = "Discount cannot exceed 100%")
    private BigDecimal discountPercent;

    @DecimalMin(value = "0", message = "Tax rate cannot be negative")
    private BigDecimal taxRate;
}
