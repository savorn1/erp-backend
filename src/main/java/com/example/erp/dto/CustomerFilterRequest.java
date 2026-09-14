package com.example.erp.dto;

import com.example.erp.entity.CustomerStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class CustomerFilterRequest {

    private String name;
    private Long companyId;
    private Long customerTypeId;
    private Long customerGroupId;
    private CustomerStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
