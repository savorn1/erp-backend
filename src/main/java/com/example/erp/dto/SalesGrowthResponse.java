package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesGrowthResponse {

    private LocalDate currentFrom;
    private LocalDate currentTo;
    private BigDecimal currentRevenue;
    private long currentOrderCount;

    private LocalDate previousFrom;
    private LocalDate previousTo;
    private BigDecimal previousRevenue;
    private long previousOrderCount;

    // Null when previousRevenue is zero (percent change is undefined).
    private BigDecimal revenueGrowthPercent;
    private BigDecimal orderGrowthPercent;
}
