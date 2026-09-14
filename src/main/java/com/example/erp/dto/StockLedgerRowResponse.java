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
public class StockLedgerRowResponse {

    private LocalDateTime date;
    private Long productId;
    private String productName;
    private String productSku;
    private Long warehouseId;
    private String warehouseName;
    private String type;
    private String referenceType;
    private Long referenceId;
    private BigDecimal quantityDelta;
    // Running balance after this row, within the filtered scope — see
    // InventoryReportServiceImpl.stockLedger.
    private BigDecimal balance;
}
