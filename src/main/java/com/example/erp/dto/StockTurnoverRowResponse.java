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
public class StockTurnoverRowResponse {

    private Long productId;
    private String productName;
    private String productSku;
    private Long warehouseId;
    private String warehouseName;
    private BigDecimal currentStock;
    // Total outbound quantity (ISSUE/TRANSFER_OUT/MATERIAL_CONSUMPTION)
    // within the trailing window.
    private BigDecimal outboundQuantityInWindow;
    private LocalDateTime lastOutboundDate;
}
