package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class LeadActivityFilterRequest {

    private String sortBy = "createdAt";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 20;
}
