package com.example.erp.dto;

import com.example.erp.entity.SerialNumberStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class SerialNumberFilterRequest {

    private String serialNumber;
    private Long companyId;
    private Long productId;
    private Long warehouseId;
    private SerialNumberStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 20;
}
