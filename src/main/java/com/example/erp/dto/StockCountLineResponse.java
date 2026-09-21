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
public class StockCountLineResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Long binId;
    private String binName;

    // The unit the counter enters quantities in, and how many base units one
    // of them makes.
    private Long unitOfMeasureId;
    private String unitOfMeasureAbbreviation;
    private BigDecimal conversionFactor;

    // The product's own inventory unit, so the UI can label the base-unit
    // figures below without having to fetch the product separately.
    private String baseUnitOfMeasureAbbreviation;

    // All three in the product's base inventory unit.
    private BigDecimal systemQuantity;
    private BigDecimal countedQuantity;
    private BigDecimal varianceQuantity;

    // The same figures expressed in unitOfMeasure, for the count sheet the
    // warehouse actually works from. Display only — never round-tripped back,
    // so the division here can't corrupt the stored base quantities.
    private BigDecimal systemQuantityInUnit;
    private BigDecimal countedQuantityInUnit;
}
