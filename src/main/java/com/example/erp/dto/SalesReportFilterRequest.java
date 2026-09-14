package com.example.erp.dto;

import com.example.erp.entity.SalesOrderStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class SalesReportFilterRequest {

    private Long companyId;
    // Both optional — omitted means "no lower/upper bound".
    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Omitted means every status except CANCELLED (see SalesReportServiceImpl).
    private SalesOrderStatus status;
}
