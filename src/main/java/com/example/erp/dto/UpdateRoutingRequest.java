package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

// companyId/bomId are deliberately not editable — mirrors
// UpdateBillOfMaterialRequest's own reasoning.
@Data
public class UpdateRoutingRequest {

    @NotBlank
    private String name;

    private String notes;

    @NotEmpty
    @Valid
    private List<RoutingOperationRequest> operations;
}
