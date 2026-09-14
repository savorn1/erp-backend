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
public class PurchaseCancellationRowResponse {

    private Long orderId;
    private String poNumber;
    private LocalDate orderDate;
    private Long supplierId;
    private String supplierName;
    // Value the order would have had, computed from its current lines — the
    // system has no cancellation-reason or cancelled-at field to report.
    private BigDecimal amount;
}
