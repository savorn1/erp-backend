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
public class ArSummaryResponse {

    private LocalDate asOfDate;
    private BigDecimal totalOutstanding;
    private BigDecimal totalOverdue;
    private long invoiceCount;
    private long customerCount;
    private long overdueInvoiceCount;
    private long overdueCustomerCount;
    // 0 when nothing is overdue.
    private long oldestOverdueDays;
}
