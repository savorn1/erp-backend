package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

// productId is optional but a ledger only reads as a running balance when
// it's set — see InventoryReportServiceImpl.stockLedger.
@Data
@ParameterObject
public class StockLedgerFilterRequest {

    private Long companyId;
    private Long warehouseId;
    private Long productId;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
