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
public class PaymentSummaryResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    private long customerPaymentCount;
    private BigDecimal customerReceived;
    private BigDecimal customerRefunded;
    // customerReceived - customerRefunded.
    private BigDecimal customerNet;
    private long supplierPaymentCount;
    private BigDecimal supplierPaid;
    private BigDecimal supplierRefunded;
    private BigDecimal supplierNet;
    // customerNet - supplierNet.
    private BigDecimal netCashFlow;
}
