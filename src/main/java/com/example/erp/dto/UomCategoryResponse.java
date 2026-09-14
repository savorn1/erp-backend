package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UomCategoryResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private boolean active;
    // The UnitOfMeasure flagged as this category's base unit, if any.
    private Long baseUnitId;
    private String baseUnitName;
    private String baseUnitAbbreviation;
}
