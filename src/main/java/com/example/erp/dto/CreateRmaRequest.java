package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateRmaRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long customerId;

    // Optional — required only for REFUND resolution later.
    private Long invoiceId;

    @NotNull
    private Long warehouseId;

    @NotNull
    private LocalDate requestDate;

    private String reason;

    private String notes;

    @NotEmpty
    @Valid
    private List<RmaLineRequest> lines;
}
