package com.example.erp.dto;

import com.example.erp.entity.SupplierStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class SupplierFilterRequest {

    private String name;
    private Long companyId;
    private Long supplierTypeId;
    private SupplierStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
