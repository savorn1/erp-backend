package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.math.BigDecimal;

@Data
@ParameterObject
public class ConvertUomRequest {

    @NotNull
    private Long productId;

    // Optional — when set, converts using that variant's own ProductUom rows
    // instead of the parent product's.
    private Long variantId;

    @NotNull
    private Long fromUnitOfMeasureId;

    @NotNull
    private Long toUnitOfMeasureId;

    @NotNull
    private BigDecimal quantity;
}
