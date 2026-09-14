package com.example.erp.dto;

import com.example.erp.entity.AccountingPeriodStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class AccountingPeriodFilterRequest {

    private Long companyId;
    private Long fiscalYearId;
    private AccountingPeriodStatus status;

    private String sortBy = "startDate";
    private String sortOrder = "asc";
    private int page = 1;
    private int size = 100;
}
