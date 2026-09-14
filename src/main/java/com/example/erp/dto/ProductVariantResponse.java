package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String name;
    private String sku;
    private String barcode;
    // Null means this variant inherits the parent product's own unit.
    private Long unitOfMeasureId;
    private String unitOfMeasureName;
    private String unitOfMeasureAbbreviation;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private String imageUrl;
    private boolean active;
}
