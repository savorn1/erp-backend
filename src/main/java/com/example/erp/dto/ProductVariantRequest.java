package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

// Used for both create and update. costPrice/sellingPrice are optional
// overrides — null means "inherit the parent product's price".
@Data
public class ProductVariantRequest {

    @NotNull
    private Long productId;

    @NotBlank
    private String name;

    @NotBlank
    private String sku;

    private String barcode;

    // Optional — leave unset to use the parent product's own unit of measure.
    private Long unitOfMeasureId;

    @DecimalMin(value = "0", message = "Cost price cannot be negative")
    private BigDecimal costPrice;

    @DecimalMin(value = "0", message = "Selling price cannot be negative")
    private BigDecimal sellingPrice;

    private String imageUrl;

    private boolean active = true;
}
