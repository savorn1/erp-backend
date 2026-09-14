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
public class BomCostRowResponse {

    private Long bomId;
    private String bomNumber;
    private String name;
    private Integer version;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal outputQuantity;
    // Sum of each component's current Product.costPrice * line quantity
    // (scaled by scrapPercent) — a live "what should this batch cost today"
    // estimate, not a historical actual.
    private BigDecimal materialCostPerBatch;
    private BigDecimal materialCostPerUnit;
}
