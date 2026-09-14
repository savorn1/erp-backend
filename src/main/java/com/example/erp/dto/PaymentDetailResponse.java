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
public class PaymentDetailResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Customer and supplier payments merged, most recent first.
    private List<PaymentDetailRowResponse> rows;
    // Net (payments minus refunds) per ledger.
    private BigDecimal totalReceived;
    private BigDecimal totalPaid;
}
