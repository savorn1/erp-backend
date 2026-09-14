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
public class StockLevelResponse {

    private Long id;
    private Long companyId;
    private Long productId;
    private String productName;
    private String productSku;
    private String unitOfMeasureAbbreviation;
    private Long warehouseId;
    private String warehouseName;
    private Long binId;
    private String binName;
    private BigDecimal quantityOnHand;
}
