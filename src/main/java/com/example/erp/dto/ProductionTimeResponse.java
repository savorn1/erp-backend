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
public class ProductionTimeResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Orders with both actualStartDate and actualEndDate set.
    private List<ProductionTimeRowResponse> rows;
    private BigDecimal averageDurationHours;
}
