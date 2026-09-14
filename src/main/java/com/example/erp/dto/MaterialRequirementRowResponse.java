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
public class MaterialRequirementRowResponse {

    private Long componentProductId;
    private String componentProductName;
    private String componentProductSku;
    private Long warehouseId;
    private String warehouseName;
    private String unitOfMeasureAbbreviation;
    // Sum of outstanding (not-yet-consumed) requirement across DRAFT/RELEASED orders.
    private BigDecimal totalRequiredQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal shortfallQuantity;
}
