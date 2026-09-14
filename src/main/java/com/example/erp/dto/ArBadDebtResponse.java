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
public class ArBadDebtResponse {

    private LocalDate asOfDate;
    private int thresholdDays;
    // Outstanding invoices overdue beyond thresholdDays — sorted by days
    // overdue, descending. Not an actual write-off list — the system has no
    // bad-debt/write-off status — just invoices old enough to flag for review.
    private List<ArBadDebtRowResponse> rows;
    private long invoiceCount;
    private BigDecimal totalAmount;
}
