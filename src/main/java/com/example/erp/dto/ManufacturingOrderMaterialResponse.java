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
public class ManufacturingOrderMaterialResponse {

    private Long id;
    private Long componentProductId;
    private String componentProductName;
    private String componentProductSku;
    private Long unitOfMeasureId;
    private String unitOfMeasureAbbreviation;
    private BigDecimal requiredQuantity;
    private BigDecimal consumedQuantity;
    private BigDecimal unitCost;
    private BigDecimal lineCost;
}
