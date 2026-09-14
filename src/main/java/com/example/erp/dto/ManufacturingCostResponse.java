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
public class ManufacturingCostResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Completed orders only, most recent first.
    private List<ManufacturingCostRowResponse> rows;
    private BigDecimal totalMaterialCost;
    private BigDecimal totalLaborCost;
    private BigDecimal totalOverheadCost;
    private BigDecimal totalCost;
}
