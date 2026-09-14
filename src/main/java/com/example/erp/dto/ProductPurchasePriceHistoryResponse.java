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
public class ProductPurchasePriceHistoryResponse {

    private Long productId;
    private String productName;
    private String productSku;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Sorted by order date, ascending — every purchase line for this
    // product, across every supplier, showing what was paid and when.
    private List<ProductPurchasePriceHistoryRowResponse> rows;
}
