package com.example.erp.dto;

import com.example.erp.entity.ProductTypeCode;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class ProductTypeFilterRequest {

    private ProductTypeCode code;

    private String name;
    private Boolean active;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
