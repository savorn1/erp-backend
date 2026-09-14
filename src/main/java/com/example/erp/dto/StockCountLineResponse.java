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
public class StockCountLineResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Long binId;
    private String binName;
    private BigDecimal systemQuantity;
    private BigDecimal countedQuantity;
    private BigDecimal varianceQuantity;
}
