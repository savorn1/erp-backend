package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDetailResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Sorted by order date, descending — capped at MAX_ROWS (see
    // PurchaseReportServiceImpl) since this is a per-line flat export.
    private List<PurchaseDetailRowResponse> rows;
    private boolean truncated;
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;
}
