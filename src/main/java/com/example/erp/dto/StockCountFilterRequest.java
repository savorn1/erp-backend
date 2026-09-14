package com.example.erp.dto;

import com.example.erp.entity.StockCountStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class StockCountFilterRequest {

    private String countNumber;
    private Long companyId;
    private Long warehouseId;
    private StockCountStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
