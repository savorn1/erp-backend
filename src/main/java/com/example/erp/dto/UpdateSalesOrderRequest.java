package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

// Only allowed while the SO is still DRAFT — see SalesOrderServiceImpl.
@Data
public class UpdateSalesOrderRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long customerId;

    @NotNull
    private Long warehouseId;

    @NotNull
    private LocalDate orderDate;

    private LocalDate expectedDate;

    private String notes;

    @NotEmpty
    @Valid
    private List<SalesOrderLineRequest> lines;
}
