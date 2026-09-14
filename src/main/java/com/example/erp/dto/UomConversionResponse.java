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
public class UomConversionResponse {

    private Long id;
    private Long fromUnitOfMeasureId;
    private String fromUnitOfMeasureName;
    private String fromUnitOfMeasureAbbreviation;
    private Long toUnitOfMeasureId;
    private String toUnitOfMeasureName;
    private String toUnitOfMeasureAbbreviation;
    private BigDecimal conversionFactor;
    private boolean active;
}
