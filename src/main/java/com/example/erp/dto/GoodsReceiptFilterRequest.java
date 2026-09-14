package com.example.erp.dto;

import com.example.erp.entity.GoodsReceiptStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class GoodsReceiptFilterRequest {

    private String receiptNumber;
    private Long companyId;
    private Long purchaseOrderId;
    private Long warehouseId;
    private GoodsReceiptStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
