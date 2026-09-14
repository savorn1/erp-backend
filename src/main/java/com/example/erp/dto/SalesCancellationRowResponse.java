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
public class SalesCancellationRowResponse {

    private Long orderId;
    private String soNumber;
    private LocalDate orderDate;
    private Long customerId;
    private String customerName;
    // Value the order would have had, computed from its current lines — the
    // system has no cancellation-reason or cancelled-at field to report.
    private BigDecimal amount;
}
