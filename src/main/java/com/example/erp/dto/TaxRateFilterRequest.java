package com.example.erp.dto;

import com.example.erp.entity.TaxType;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class TaxRateFilterRequest {

    private String search;
    private Long companyId;
    private TaxType type;
    private Boolean active;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
