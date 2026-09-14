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

    @NotNull
    @DecimalMin(value = "0", message = "Price cannot be negative")
    private BigDecimal price;
}
