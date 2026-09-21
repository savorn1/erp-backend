package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductPriceRequest {

    @NotNull
    private Long productId;

    @NotNull
    private Long priceGroupId;

    // Omit (or name the product's own base unit) for the base price. Any
    // other unit must be a sales-allowed ProductUom on that product, and the
    // price given is per one of *that* unit — it is never scaled.
    private Long unitOfMeasureId;

    @NotNull
    @DecimalMin(value = "0", message = "Price cannot be negative")
    private BigDecimal price;
}
