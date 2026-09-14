package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

// Reused loosely across every ManufacturingReportController endpoint even
// though each report only reads a subset of these fields — same precedent as
// SalesReportFilterRequest.
@Data
@ParameterObject
public class ManufacturingReportFilterRequest {

    private Long companyId;
    private Long warehouseId;
    private Long productId;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
