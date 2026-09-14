package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateStockTransferRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long sourceWarehouseId;

    @NotNull
    private Long destinationWarehouseId;

    @NotNull
    private LocalDate requestDate;

    private String notes;

    @NotEmpty
    @Valid
    private List<StockTransferLineRequest> lines;
}
