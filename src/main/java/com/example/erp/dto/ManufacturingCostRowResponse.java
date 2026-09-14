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
public class ManufacturingCostRowResponse {

    private String moNumber;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal producedQuantity;
    private BigDecimal materialCost;
    private BigDecimal laborCost;
    private BigDecimal overheadCost;
    private BigDecimal totalCost;
    private BigDecimal unitCost;
}
