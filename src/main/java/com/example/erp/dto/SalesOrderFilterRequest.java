package com.example.erp.dto;

import com.example.erp.entity.SalesOrderStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class SalesOrderFilterRequest {

    private String soNumber;
    private Long companyId;
    private Long customerId;
    private Long warehouseId;
    private SalesOrderStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
