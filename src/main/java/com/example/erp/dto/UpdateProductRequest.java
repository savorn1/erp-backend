package com.example.erp.dto;

import com.example.erp.entity.ProductTrackingType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

// Deliberately excludes `status` — that has its own endpoint (see
// UpdateProductStatusRequest), same reasoning as Company/Branch/Department.
@Data
public class UpdateProductRequest {

    @NotNull
    private Long companyId;

    private Long categoryId;
    private Long brandId;
    private Long typeId;

    @NotNull
    private Long unitOfMeasureId;

    private Long supplierId;

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String sku;

    private String barcode;

    @NotNull
    @DecimalMin(value = "0", message = "Cost price cannot be negative")
    private BigDecimal costPrice;

    @NotNull
    @DecimalMin(value = "0", message = "Selling price cannot be negative")
    private BigDecimal sellingPrice;

    @DecimalMin(value = "0", message = "Tax rate cannot be negative")
    @DecimalMax(value = "100", message = "Tax rate cannot exceed 100%")
    private BigDecimal taxRate;

    private ProductTrackingType trackingType;

    private String imageUrl;

    // Optional — leave unset (or zero) to never flag this product on the Low
    // Stock report.
    @DecimalMin(value = "0", message = "Reorder point cannot be negative")
    private BigDecimal reorderPoint;

    // Optional — leave unset (or zero) to never flag this product on the
    // Overstock report.
    @DecimalMin(value = "0", message = "Max stock cannot be negative")
    private BigDecimal maxStock;

    private Integer warrantyMonths;
}
