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
public class TaxDetailResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Every taxed line from both approved sales invoices (Output) and
    // approved purchase invoices (Input) — sorted by date, descending.
    private List<TaxDetailRowResponse> rows;
    private BigDecimal totalOutputTax;
    private BigDecimal totalInputTax;
}
