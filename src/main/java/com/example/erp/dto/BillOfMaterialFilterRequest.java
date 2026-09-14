package com.example.erp.dto;

import com.example.erp.entity.BillOfMaterialStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class BillOfMaterialFilterRequest {

    private String bomNumber;
    private Long companyId;
    private Long productId;
    private BillOfMaterialStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
