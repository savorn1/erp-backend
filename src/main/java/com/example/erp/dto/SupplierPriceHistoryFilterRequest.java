package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class SupplierPriceHistoryFilterRequest {

    private Long companyId;
    private Long supplierId;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
