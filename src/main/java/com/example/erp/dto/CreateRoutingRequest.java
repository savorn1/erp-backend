package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateRoutingRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long bomId;

    @NotBlank
    private String name;

    private String notes;

    @NotEmpty
    @Valid
    private List<RoutingOperationRequest> operations;
}
