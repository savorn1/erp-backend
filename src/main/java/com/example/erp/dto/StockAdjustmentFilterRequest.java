package com.example.erp.dto;

import com.example.erp.entity.StockAdjustmentStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class StockAdjustmentFilterRequest {

    private String adjustmentNumber;
    private Long companyId;
    private Long warehouseId;
    private StockAdjustmentStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
