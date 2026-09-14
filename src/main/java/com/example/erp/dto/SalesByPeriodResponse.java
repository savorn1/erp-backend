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
public class SalesByPeriodResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Sorted chronologically, ascending — one entry per period even when a
    // period has no orders (zero-filled), like DashboardServiceImpl.trend().
    private List<SalesByPeriodRowResponse> rows;
    private BigDecimal totalRevenue;
}
