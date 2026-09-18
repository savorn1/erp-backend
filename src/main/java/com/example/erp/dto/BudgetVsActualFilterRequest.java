package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class BudgetVsActualFilterRequest {

    private Long companyId;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private Long costCenterId;
}
