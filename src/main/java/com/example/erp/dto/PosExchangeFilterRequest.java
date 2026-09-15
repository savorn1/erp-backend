package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class PosExchangeFilterRequest {

    private String exchangeNumber;
    private Long companyId;
    private Long registerId;
    private Long originalPosSaleId;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
