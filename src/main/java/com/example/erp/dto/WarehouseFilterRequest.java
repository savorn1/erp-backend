package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class WarehouseFilterRequest {

    private String name;
    private Long companyId;
    private Boolean active;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
