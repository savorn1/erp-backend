package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// One StockCountLine, flattened with its parent StockCount's header fields
// — backs both Inventory Count (grouped by stockCountId client-side) and
// Stock Variance (filtered to varianceQuantity != 0).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockCountVarianceRowResponse {

    private Long stockCountId;
    private String countNumber;
    private LocalDate countDate;
    private String status;
    private Long warehouseId;
    private String warehouseName;
    private Long productId;
    private String productName;
    private String productSku;
    private Long binId;
    private String binName;
    private BigDecimal systemQuantity;
    private BigDecimal countedQuantity;
    private BigDecimal varianceQuantity;
}
