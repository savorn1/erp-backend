package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class BudgetFilterRequest {

    private Long companyId;
    private Long fiscalYearId;
    private Long accountingPeriodId;
    private Long costCenterId;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
