package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class CommissionReportFilterRequest {

    private Long companyId;
    // Both optional — omitted means "no lower/upper bound" on earnedDate
    // (the date the underlying payment was recorded).
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private Long salesRepUserId;
}
