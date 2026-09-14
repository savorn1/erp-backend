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
public class StockAgingRowResponse {

    private Long productId;
    private String productName;
    private String productSku;
    private Long warehouseId;
    private String warehouseName;
    private BigDecimal currentStock;
    private BigDecimal value;
    // Null when this product/warehouse has no recorded inbound movement
    // (e.g. its stock predates StockMovement tracking) — bucketed as "Unknown".
    private LocalDateTime lastInboundDate;
    private Integer daysSinceInbound;
    // One of: "0-30", "31-60", "61-90", "90+", "Unknown".
    private String ageBucket;
}
