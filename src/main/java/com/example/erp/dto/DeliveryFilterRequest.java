package com.example.erp.dto;

import com.example.erp.entity.DeliveryStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class DeliveryFilterRequest {

    private String deliveryNumber;
    private Long companyId;
    private Long salesOrderId;
    private Long warehouseId;
    private DeliveryStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
