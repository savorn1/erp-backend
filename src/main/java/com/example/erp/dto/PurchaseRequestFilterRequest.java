package com.example.erp.dto;

import com.example.erp.entity.PurchaseRequestStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class PurchaseRequestFilterRequest {

    private String requestNumber;
    private Long companyId;
    private Long departmentId;
    private PurchaseRequestStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
