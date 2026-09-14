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
public class WorkCenterUtilizationRowResponse {

    private Long workCenterId;
    private String workCenterName;
    private long operationCount;
    private BigDecimal totalActualHours;
    private BigDecimal averageHoursPerOperation;
    // totalActualHours / wall-clock hours in the filtered period * 100 — a
    // naive utilization estimate, since this codebase has no shift/business-
    // calendar concept to compute "available" hours against. Null when the
    // filter doesn't specify both dateFrom and dateTo.
    private BigDecimal utilizationPercent;
}
