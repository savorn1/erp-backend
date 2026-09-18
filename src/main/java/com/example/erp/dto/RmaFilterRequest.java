package com.example.erp.dto;

import com.example.erp.entity.RmaStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class RmaFilterRequest {

    private Long companyId;
    private Long customerId;
    private RmaStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
