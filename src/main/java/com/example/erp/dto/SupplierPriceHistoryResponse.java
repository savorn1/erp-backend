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
public class SupplierPriceHistoryResponse {

    private Long supplierId;
    private String supplierName;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Sorted by order date, ascending — every purchase line from this
    // supplier, across every product, showing what was paid and when.
    private List<SupplierPriceHistoryRowResponse> rows;
}
