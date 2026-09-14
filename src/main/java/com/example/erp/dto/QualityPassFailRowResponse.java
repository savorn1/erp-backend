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
public class QualityPassFailRowResponse {

    private Long productId;
    private String productName;
    private String productSku;
    private long inspectedCount;
    private long passedCount;
    private long failedCount;
    private BigDecimal passRatePercent;
}
