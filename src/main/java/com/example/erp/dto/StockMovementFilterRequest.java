package com.example.erp.dto;

import com.example.erp.entity.StockMovementType;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class StockMovementFilterRequest {

    private Long companyId;
    private Long productId;
    private Long warehouseId;
    private StockMovementType type;
    // Both optional — compared against createdAt's date component.
    private LocalDate dateFrom;
    private LocalDate dateTo;

    private String sortBy = "createdAt";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 20;
}
