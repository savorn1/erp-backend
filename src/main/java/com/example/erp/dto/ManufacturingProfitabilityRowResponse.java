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
public class ManufacturingProfitabilityRowResponse {

    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal totalProducedQuantity;
    // totalCost / totalProducedQuantity across matching completed orders.
    private BigDecimal averageUnitCost;
    // Product's current selling price.
    private BigDecimal sellingPrice;
    private BigDecimal marginPerUnit;
    private BigDecimal totalMargin;
    private BigDecimal marginPercent;
}
