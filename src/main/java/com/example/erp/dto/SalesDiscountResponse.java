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
public class SalesDiscountResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Only lines with discountPercent > 0, sorted by discount amount, descending.
    private List<SalesDiscountRowResponse> rows;
    private BigDecimal totalDiscountAmount;
}
