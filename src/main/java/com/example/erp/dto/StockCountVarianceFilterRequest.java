package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class StockCountVarianceFilterRequest {

    private Long companyId;
    private Long warehouseId;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
