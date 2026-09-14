package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomComparisonSideResponse {

    private Long bomId;
    private String bomNumber;
    private Integer version;
    private String name;
    private String status;
    private BigDecimal outputQuantity;
    private BigDecimal materialCostPerBatch;
    private BigDecimal materialCostPerUnit;
    private List<BomComparisonLineResponse> lines;
}
