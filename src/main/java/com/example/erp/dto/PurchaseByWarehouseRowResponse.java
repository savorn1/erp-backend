package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseByWarehouseRowResponse {

    private Long warehouseId;
    private String warehouseName;
    private long orderCount;
    private BigDecimal amount;
}
