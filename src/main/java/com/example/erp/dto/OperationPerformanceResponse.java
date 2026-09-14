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
public class OperationPerformanceResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Grouped by (operation name, work center) — completed work orders only.
    private List<OperationPerformanceRowResponse> rows;
}
