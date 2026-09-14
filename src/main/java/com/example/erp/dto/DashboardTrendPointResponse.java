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
public class DashboardTrendPointResponse {

    // "YYYY-MM".
    private String month;
    private BigDecimal sales;
    private BigDecimal purchase;
}
