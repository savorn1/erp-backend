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
public class RefundReportResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // REFUND-type rows from both ledgers, most recent first.
    private List<RefundRowResponse> rows;
    private BigDecimal totalCustomerRefunds;
    private BigDecimal totalSupplierRefunds;
}
