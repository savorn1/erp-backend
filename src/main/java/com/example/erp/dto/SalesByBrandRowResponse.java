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
public class SalesByBrandRowResponse {

    // Null when the product has no brand assigned.
    private Long brandId;
    private String brandName;
    private BigDecimal quantity;
    private BigDecimal revenue;
}
