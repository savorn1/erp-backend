package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class GenerateFromLowStockRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long departmentId;

    @NotNull
    private LocalDate requestDate;

    // Narrows the Low Stock sweep to one warehouse — omit to consider every
    // warehouse for the company.
    private Long warehouseId;

    // Omit to reorder every currently-low product for the company/warehouse;
    // set to reorder only specific products (the "create PR" action on a
    // single Low Stock row uses this with one id).
    private List<Long> productIds;
}
