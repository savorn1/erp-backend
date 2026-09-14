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
public class MoSummaryResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // One row per status.
    private List<MoSummaryRowResponse> rows;
    private long totalOrders;
    private BigDecimal totalPlannedQuantity;
    private BigDecimal totalProducedQuantity;
    private BigDecimal totalScrapQuantity;
}
