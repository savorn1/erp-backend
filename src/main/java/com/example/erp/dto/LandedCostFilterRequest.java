package com.example.erp.dto;

import com.example.erp.entity.LandedCostType;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class LandedCostFilterRequest {

    private Long companyId;
    private Long goodsReceiptId;
    private LandedCostType costType;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
