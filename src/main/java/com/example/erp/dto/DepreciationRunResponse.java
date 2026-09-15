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
public class DepreciationRunResponse {

    private Long id;
    private Long companyId;
    private Long accountingPeriodId;
    private String accountingPeriodName;
    private LocalDate runDate;
    private Long journalEntryId;
    private String journalNumber;
    private BigDecimal totalAmount;
    private int assetCount;
    private String createdBy;
    private List<DepreciationEntryResponse> entries;
}
