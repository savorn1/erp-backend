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
public class ProductResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private Long typeId;
    private String typeName;
    private Long unitOfMeasureId;
    private String unitOfMeasureName;
    private String unitOfMeasureAbbreviation;
    // The unit this product's defaultPurchase/defaultSales ProductUom row
    // points to — falls back to unitOfMeasureId/Abbreviation above when no
    // ProductUom rows have been configured for it yet (see ProductServiceImpl).
    private Long purchaseUnitOfMeasureId;
    private String purchaseUnitOfMeasureAbbreviation;
    private Long salesUnitOfMeasureId;
    private String salesUnitOfMeasureAbbreviation;
    private Long supplierId;
    private String supplierName;
    private String name;
    private String description;
    private String sku;
    private String barcode;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private BigDecimal taxRate;
    private String status;
    private String trackingType;
    private String imageUrl;
    // Null/zero means no threshold configured — never flagged by the Low
    // Stock report.
    private BigDecimal reorderPoint;
    // Null/zero means no threshold configured — never flagged by the
    // Overstock report.
    private BigDecimal maxStock;
    private Integer warrantyMonths;
}
