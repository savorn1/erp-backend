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
public class PurchaseOrderLineResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Long unitOfMeasureId;
    private String unitOfMeasureAbbreviation;
    // How many of the product's base/inventory unit equal 1 of the unit
    // above — snapshotted at order time (see PurchaseOrderLine.conversionFactor).
    private BigDecimal conversionFactor;
    private BigDecimal quantityOrdered;
    private BigDecimal unitCost;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal quantityReceived;
    // quantityOrdered/quantityReceived converted to the product's base/
    // inventory unit — what actually lands in stock.
    private BigDecimal baseQuantityOrdered;
    private BigDecimal baseQuantityReceived;
    // Net of discount, inclusive of tax.
    private BigDecimal lineTotal;
}
