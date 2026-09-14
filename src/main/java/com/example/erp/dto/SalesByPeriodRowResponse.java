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
public class SalesByPeriodRowResponse {

    // "YYYY-MM" for the monthly report, "YYYY" for the yearly one.
    private String period;
    private long orderCount;
    private BigDecimal revenue;
}
