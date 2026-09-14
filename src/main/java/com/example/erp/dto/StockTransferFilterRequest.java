package com.example.erp.dto;

import com.example.erp.entity.StockTransferStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class StockTransferFilterRequest {

    private String transferNumber;
    private Long companyId;
    private Long sourceWarehouseId;
    private Long destinationWarehouseId;
    private StockTransferStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
