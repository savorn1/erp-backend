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
public class PlanVsActualResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Completed orders only.
    private List<PlanVsActualRowResponse> rows;
    private BigDecimal averageAchievementPercent;
    private BigDecimal averageYieldPercent;
}
