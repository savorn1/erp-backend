package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class StockTurnoverFilterRequest {

    private Long companyId;
    private Long warehouseId;
    private Long productId;

    // Trailing window, in days, used to measure outbound activity.
    private int days = 90;
}
