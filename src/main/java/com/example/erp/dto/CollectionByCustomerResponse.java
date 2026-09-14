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
public class CollectionByCustomerResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Sorted by net, descending.
    private List<CollectionByCustomerRowResponse> rows;
    private BigDecimal totalNet;
}
