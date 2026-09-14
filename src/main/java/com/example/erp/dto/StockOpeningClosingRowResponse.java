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
public class StockOpeningClosingRowResponse {

    private Long productId;
    private String productName;
    private String productSku;
    private Long warehouseId;
    private String warehouseName;
    // Balance immediately before dateFrom (0 when dateFrom is unset).
    private BigDecimal openingQuantity;
    // Balance as of the end of dateTo (now, when dateTo is unset).
    private BigDecimal closingQuantity;
}
