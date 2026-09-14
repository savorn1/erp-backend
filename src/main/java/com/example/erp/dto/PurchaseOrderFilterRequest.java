package com.example.erp.dto;

import com.example.erp.entity.PurchaseOrderStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class PurchaseOrderFilterRequest {

    private String poNumber;
    private Long companyId;
    private Long supplierId;
    private Long warehouseId;
    private PurchaseOrderStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
