package com.example.erp.dto;

import com.example.erp.entity.FiscalYearStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class FiscalYearFilterRequest {

    private Long companyId;
    private FiscalYearStatus status;

    private String sortBy = "startDate";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
