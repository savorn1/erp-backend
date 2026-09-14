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
public class MaterialConsumptionResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Sorted by totalCost, descending.
    private List<MaterialConsumptionRowResponse> rows;
    private BigDecimal totalConsumedCost;
}
