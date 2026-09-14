package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class UomConversionFilterRequest {

    // Matches rows where this unit is either the "from" or the "to" side.
    private Long unitOfMeasureId;
    private Boolean active;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
