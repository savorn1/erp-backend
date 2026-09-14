package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapWastageRowResponse {

    private String moNumber;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal plannedQuantity;
    private BigDecimal producedQuantity;
    private BigDecimal scrapQuantity;
    // scrapQuantity / (producedQuantity + scrapQuantity) * 100.
    private BigDecimal scrapPercent;
    private String scrapReason;
    private LocalDateTime actualEndDate;
}
