package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// Used for both create and update.
@Data
public class ProductUomPriceRequest {

    @NotNull
    private Long priceGroupId;

    @NotNull
    @DecimalMin(value = "0", message = "Price cannot be negative")
    private BigDecimal price;

    // Optional bounds — null on either side means no start/end limit.
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    private boolean active = true;
}
