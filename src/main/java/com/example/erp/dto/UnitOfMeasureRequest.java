package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UnitOfMeasureRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String abbreviation;

    private String description;

    // Whether a fractional quantity is valid for this unit, e.g. 2.5 KG is
    // fine, 2.5 PCS is not. Defaults to true (most units allow decimals).
    private boolean decimalAllowed = true;

    private boolean active = true;

    // Optional — leave unset for a standalone unit with no generic conversion.
    private Long categoryId;

    // Ignored when categoryId is unset. Setting this true unsets any other
    // base unit already flagged in the same category — see
    // UnitOfMeasureServiceImpl.setCategoryBaseUnit. Conversion factors
    // against the base unit are managed separately — see UomConversion /
    // UomConversionService.
    private boolean baseUnit;
}
