package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreatePurchaseOrderRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long supplierId;

    @NotNull
    private Long warehouseId;

    @NotNull
    private LocalDate orderDate;

    private LocalDate expectedDate;

    private String notes;

    @NotEmpty
    @Valid
    private List<PurchaseOrderLineRequest> lines;
}
