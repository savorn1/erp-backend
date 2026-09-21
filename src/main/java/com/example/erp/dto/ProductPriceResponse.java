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
public class ProductPriceResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Long priceGroupId;
    private String priceGroupName;
    private BigDecimal price;

    // The unit this price is for. Null means the product's base unit, which
    // is also what every row written before per-unit pricing meant.
    private Long unitOfMeasureId;

    // Label for the unit the price is per — the priced unit when there is
    // one, otherwise the product's base unit. Without it a bare number on the
    // list view says nothing about what it buys.
    private String unitOfMeasureAbbreviation;

    // True when this row prices a non-base unit, so the UI can mark it as a
    // per-unit price rather than a scaled one.
    private boolean perUnitPrice;
}
