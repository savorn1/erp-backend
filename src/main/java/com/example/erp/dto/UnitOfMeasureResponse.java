package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasureResponse {

    private Long id;
    private String name;
    private String abbreviation;
    private String description;
    private boolean decimalAllowed;
    private boolean active;
    private Long categoryId;
    private String categoryName;
    private boolean baseUnit;
}
