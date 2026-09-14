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
public class BomComparisonLineResponse {

    private Long componentProductId;
    private String componentProductName;
    private String componentProductSku;
    private BigDecimal quantity;
    private BigDecimal scrapPercent;
    private BigDecimal lineCost;
}
