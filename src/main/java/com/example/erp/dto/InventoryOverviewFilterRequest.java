package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class InventoryOverviewFilterRequest {

    private Long companyId;
    private Long warehouseId;
    private Long productId;
    private String search;

    private String sortBy = "productName";
    private String sortOrder = "asc";
    private int page = 1;
    private int size = 10;
}
