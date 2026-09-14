package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostVarianceResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Orders that have at least started production (materialCost stamped).
    private List<CostVarianceRowResponse> rows;
    private BigDecimal totalStandardMaterialCost;
    private BigDecimal totalActualMaterialCost;
    private BigDecimal totalVarianceAmount;
}
