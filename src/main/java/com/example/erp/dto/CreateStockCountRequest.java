package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateStockCountRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long warehouseId;

    @NotNull
    private LocalDate countDate;

    private String notes;

    @NotEmpty
    @Valid
    private List<StockCountLineRequest> lines;
}
