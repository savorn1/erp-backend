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
public class SupplierPerformanceResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Sorted by on-time percent, descending (nulls last).
    private List<SupplierPerformanceRowResponse> rows;
}
