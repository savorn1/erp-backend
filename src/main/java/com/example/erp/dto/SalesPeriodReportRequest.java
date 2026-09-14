package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class SalesPeriodReportRequest {

    private Long companyId;
    // Defines the "current" period; the "previous" period is the same
    // number of days immediately before dateFrom. Both required.
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
