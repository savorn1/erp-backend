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
public class QuotationLineResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Long unitOfMeasureId;
    private String unitOfMeasureAbbreviation;
    private BigDecimal conversionFactor;
    private BigDecimal quantity;
    // quantity restated in the product's inventory unit.
    private BigDecimal baseQuantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
