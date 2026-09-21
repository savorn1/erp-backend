package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RfqLineRequest {

    @NotNull
    private Long productId;

    // Optional. Omit for the product's own base unit; any other value must
    // already be registered as a purchase-allowed ProductUom for this product.
    private Long unitOfMeasureId;

    @NotNull
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than zero")
    private BigDecimal quantity;
}
