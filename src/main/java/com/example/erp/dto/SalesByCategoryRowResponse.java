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
public class SalesByCategoryRowResponse {

    // Null when the product has no category assigned.
    private Long categoryId;
    private String categoryName;
    private BigDecimal quantity;
    private BigDecimal revenue;
}
