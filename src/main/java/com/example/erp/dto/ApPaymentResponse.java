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
public class ApPaymentResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // PAYMENT type only (REFUND excluded) — sorted by payment date, descending.
    private List<ApPaymentRowResponse> rows;
    private BigDecimal totalPaid;
}
