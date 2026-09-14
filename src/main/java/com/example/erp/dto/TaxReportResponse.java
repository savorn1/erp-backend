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
public class TaxReportResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Tax collected on approved sales invoices, by rate.
    private List<TaxReportRateRowResponse> outputTax;
    private BigDecimal outputTaxTotal;
    // Tax paid on approved purchase invoices, by rate.
    private List<TaxReportRateRowResponse> inputTax;
    private BigDecimal inputTaxTotal;
    // outputTaxTotal - inputTaxTotal — positive means tax owed to the
    // authority, negative means a refund/credit position.
    private BigDecimal netTaxPayable;
}
