package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateRfqRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long warehouseId;

    // Optional traceability back to the PurchaseRequest this Rfq was raised
    // from — the frontend pre-fills lines from it but this id isn't
    // otherwise validated server-side beyond existing.
    private Long purchaseRequestId;

    @NotNull
    private LocalDate issueDate;

    private String notes;

    @NotEmpty
    private List<Long> supplierIds;

    @NotEmpty
    @Valid
    private List<RfqLineRequest> lines;
}
