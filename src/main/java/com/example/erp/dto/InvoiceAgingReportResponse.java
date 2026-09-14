package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceAgingReportResponse {

    private LocalDate asOfDate;
    private List<InvoiceAgingRowResponse> rows;
    private InvoiceAgingRowResponse totals;
}
