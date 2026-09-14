package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreatePurchaseRequestRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long departmentId;

    @NotNull
    private LocalDate requestDate;

    private LocalDate requiredDate;

    private String notes;

    @NotEmpty
    @Valid
    private List<PurchaseRequestLineRequest> lines;
}
