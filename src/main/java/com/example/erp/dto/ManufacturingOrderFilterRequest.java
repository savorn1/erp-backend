package com.example.erp.dto;

import com.example.erp.entity.ManufacturingOrderStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class ManufacturingOrderFilterRequest {

    private String moNumber;
    private Long companyId;
    private Long warehouseId;
    private Long productId;
    private Long productionPlanId;
    private ManufacturingOrderStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
