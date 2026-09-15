package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class PosSaleFilterRequest {

    private String saleNumber;
    private Long companyId;
    private Long registerId;
    private Long posSessionId;
    private String status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
