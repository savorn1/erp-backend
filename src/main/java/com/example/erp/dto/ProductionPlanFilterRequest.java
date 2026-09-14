package com.example.erp.dto;

import com.example.erp.entity.ProductionPlanStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class ProductionPlanFilterRequest {

    private String planNumber;
    private Long companyId;
    private ProductionPlanStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
