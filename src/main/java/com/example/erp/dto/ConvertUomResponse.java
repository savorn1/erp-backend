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
public class ConvertUomResponse {

    private Long productId;
    private Long variantId;
    private Long fromUnitOfMeasureId;
    private BigDecimal quantity;
    private Long toUnitOfMeasureId;
    private BigDecimal convertedQuantity;
    // The product's base unit and the quantity expressed in it — shown as
    // the intermediate step, since every conversion here goes through it.
    private Long baseUnitOfMeasureId;
    private BigDecimal baseQuantity;
}
