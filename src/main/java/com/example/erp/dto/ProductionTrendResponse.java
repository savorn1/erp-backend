package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionTrendResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Sorted chronologically. Based on completed orders' actualEndDate.
    private List<ProductionTrendRowResponse> rows;
}
