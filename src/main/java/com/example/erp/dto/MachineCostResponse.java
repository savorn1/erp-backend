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
public class MachineCostResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Machines without a costPerHour set are still listed (with totalCost
    // null) rather than silently dropped.
    private List<MachineCostRowResponse> rows;
    private BigDecimal totalCost;
}
