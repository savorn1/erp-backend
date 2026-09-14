package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class ProfitAndLossFilterRequest {

    private Long companyId;
    // Both optional — omitted means "no lower/upper bound" (all-time).
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
