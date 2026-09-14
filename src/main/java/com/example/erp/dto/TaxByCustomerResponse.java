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
public class TaxByCustomerResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Output tax (sales invoices) only — sorted by tax amount, descending.
    private List<TaxByCustomerRowResponse> rows;
    private BigDecimal totalTaxAmount;
}
