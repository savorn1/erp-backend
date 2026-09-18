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
public class BudgetResponse {

    private Long id;
    private Long companyId;
    private Long accountId;
    private String accountCode;
    private String accountName;
    private Long costCenterId;
    private String costCenterName;
    private Long accountingPeriodId;
    private String periodName;
    private BigDecimal amount;
    private String notes;
    private String createdBy;
}
