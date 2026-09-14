package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUomPriceResponse {

    private Long id;
    private Long productUomId;
    private Long priceGroupId;
    private String priceGroupName;
    private BigDecimal price;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private boolean active;
}
