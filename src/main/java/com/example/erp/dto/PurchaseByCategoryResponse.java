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
public class PurchaseByCategoryResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List<PurchaseByCategoryRowResponse> rows;
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;
}
