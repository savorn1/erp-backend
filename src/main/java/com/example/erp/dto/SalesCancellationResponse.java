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
public class SalesCancellationResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Sorted by order date, descending.
    private List<SalesCancellationRowResponse> rows;
    private long orderCount;
    private BigDecimal totalAmount;
}
