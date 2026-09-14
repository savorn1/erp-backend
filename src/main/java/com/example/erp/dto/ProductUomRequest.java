package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

// Used for both create and update. Ignored (forced by the server) when
// unitOfMeasureId matches the product's base unit: conversionFactor becomes
// 1 and baseUnit becomes true regardless of what's submitted here — see
// ProductUomServiceImpl.
@Data
public class ProductUomRequest {

    @NotNull
    private Long unitOfMeasureId;

    // Required unless this row is the product's base unit.
    @DecimalMin(value = "0.000001", message = "Conversion factor must be greater than zero")
    private BigDecimal conversionFactor;

    private boolean allowPurchase = true;
    private boolean allowSales = true;
    private boolean allowInventory = true;
    private boolean defaultPurchase;
    private boolean defaultSales;

    private String barcode;

    @DecimalMin(value = "0", message = "Price cannot be negative")
    private BigDecimal price;

    private boolean active = true;
}
