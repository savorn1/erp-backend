package com.example.erp.dto;

import com.example.erp.entity.QuotationStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class QuotationFilterRequest {

    private String quotationNumber;
    private Long companyId;
    private Long opportunityId;
    private Long customerId;
    private QuotationStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
