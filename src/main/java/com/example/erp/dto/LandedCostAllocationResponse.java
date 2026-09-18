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
public class LandedCostAllocationResponse {

    private Long productId;
    private String productName;
    private String productSku;
    private Long goodsReceiptLineId;
    private BigDecimal quantity;
    private BigDecimal originalUnitCost;
    private BigDecimal allocatedAmount;
    private BigDecimal newUnitCost;
}
