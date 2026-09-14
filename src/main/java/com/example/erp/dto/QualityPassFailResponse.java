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
public class QualityPassFailResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List<QualityPassFailRowResponse> rows;
    private long totalInspected;
    private long totalPassed;
    private long totalFailed;
    private BigDecimal overallPassRatePercent;
}
