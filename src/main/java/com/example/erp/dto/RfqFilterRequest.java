package com.example.erp.dto;

import com.example.erp.entity.RfqStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class RfqFilterRequest {

    private String rfqNumber;
    private Long companyId;
    private Long warehouseId;
    private RfqStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
