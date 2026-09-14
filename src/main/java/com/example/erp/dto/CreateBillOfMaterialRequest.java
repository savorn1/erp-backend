package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.math.BigDecimal;

@Data
public class CreateBillOfMaterialRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long productId;

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin(value = "0.0001", message = "Output quantity must be greater than zero")
    private BigDecimal outputQuantity;

    private String notes;

    @NotEmpty
    @Valid
    private List<BillOfMaterialLineRequest> lines;
}
