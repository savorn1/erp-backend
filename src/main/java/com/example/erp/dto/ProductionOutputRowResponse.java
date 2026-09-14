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
public class ProductionOutputRowResponse {

    private Long productId;
    private String productName;
    private String productSku;
    private long orderCount;
    private BigDecimal totalProducedQuantity;
    private BigDecimal totalScrapQuantity;
}
