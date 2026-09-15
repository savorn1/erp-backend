package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepreciationEntryResponse {

    private Long id;
    private Long depreciationRunId;
    private LocalDate runDate;
    private Long accountingPeriodId;
    private String accountingPeriodName;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private BigDecimal amount;
    private BigDecimal accumulatedAfter;
}
