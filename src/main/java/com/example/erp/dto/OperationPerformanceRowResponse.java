package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationPerformanceRowResponse {

    private String operationName;
    private String workCenterName;
    private long executionCount;
    private BigDecimal standardTimeMinutes;
    private BigDecimal averageActualTimeMinutes;
    // average actual - standard.
    private BigDecimal varianceMinutes;
}
