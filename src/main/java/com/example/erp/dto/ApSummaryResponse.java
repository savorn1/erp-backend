package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApSummaryResponse {

    private LocalDate asOfDate;
    private BigDecimal totalOutstanding;
    private BigDecimal totalOverdue;
    private long invoiceCount;
    private long supplierCount;
    private long overdueInvoiceCount;
    private long overdueSupplierCount;
    // 0 when nothing is overdue.
    private long oldestOverdueDays;
}
