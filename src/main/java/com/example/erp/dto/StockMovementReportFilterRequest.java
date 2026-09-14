package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

// Shared filter for date-ranged stock movement reports (Stock In, Stock Out,
// Stock Opening, Stock Closing, Stock Profitability, Inventory Performance).
@Data
@ParameterObject
public class StockMovementReportFilterRequest {

    private Long companyId;
    private Long warehouseId;
    private Long productId;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
