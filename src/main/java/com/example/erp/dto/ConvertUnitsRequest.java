package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.math.BigDecimal;

// Generic, product-independent unit conversion — see
// UomConversionServiceImpl.convert. Complements ConvertUomRequest, which
// converts through a specific product's own configured UOM rows.
@Data
@ParameterObject
public class ConvertUnitsRequest {

    @NotNull
    private Long fromUnitOfMeasureId;

    @NotNull
    private Long toUnitOfMeasureId;

    @NotNull
    private BigDecimal quantity;
}
