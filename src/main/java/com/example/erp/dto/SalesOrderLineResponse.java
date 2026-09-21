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
public class SalesOrderLineResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Long unitOfMeasureId;
    private String unitOfMeasureAbbreviation;
    // How many base units equal 1 of unitOfMeasureId — 1 when the line is
    // already in the product's base unit.
    private BigDecimal conversionFactor;
    private BigDecimal quantityOrdered;
    // quantityOrdered/quantityDelivered restated in the product's inventory
    // unit, so callers comparing against stock don't have to convert.
    private BigDecimal baseQuantityOrdered;
    private BigDecimal baseQuantityDelivered;
    private BigDecimal unitPrice;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal quantityDelivered;
    // Net of discount, inclusive of tax.
    private BigDecimal lineTotal;
    private BigDecimal backorderedQuantity;
}
