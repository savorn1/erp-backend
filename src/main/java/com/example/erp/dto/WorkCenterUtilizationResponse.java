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
public class WorkCenterUtilizationResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Completed work orders only, sorted by totalActualHours descending.
    private List<WorkCenterUtilizationRowResponse> rows;
}
