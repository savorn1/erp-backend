package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

// Only allowed while the Rfq is still DRAFT (see RfqServiceImpl).
@Data
public class UpdateRfqRequest {

    @NotNull
    private Long warehouseId;

    @NotNull
    private LocalDate issueDate;

    private String notes;

    @NotEmpty
    private List<Long> supplierIds;

    @NotEmpty
    @Valid
    private List<RfqLineRequest> lines;
}
