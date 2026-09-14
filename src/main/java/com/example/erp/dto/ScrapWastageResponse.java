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
public class ScrapWastageResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Completed orders with scrapQuantity > 0 only, most recent first.
    private List<ScrapWastageRowResponse> rows;
    private BigDecimal totalScrapQuantity;
}
