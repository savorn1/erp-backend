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
public class ConvertUnitsResponse {

    private Long fromUnitOfMeasureId;
    private BigDecimal quantity;
    private Long toUnitOfMeasureId;
    private BigDecimal convertedQuantity;
}
